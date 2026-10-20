package com.abos.server.module.finance.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.finance.entity.DepositRecord;
import com.abos.server.module.finance.mapper.DepositRecordMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DepositService {

    private static final Set<String> KINDS = Set.of(DepositRecord.DEPOSIT, DepositRecord.PLEDGE);

    private final DepositRecordMapper depositMapper;
    private final CustomerMapper customerMapper;
    private final FinanceSupport support;

    public java.util.List<DepositRecord> list(String kind, String status) {
        java.util.List<DepositRecord> rows = depositMapper.selectList(Wrappers.<DepositRecord>lambdaQuery()
                .eq(StringUtils.hasText(kind), DepositRecord::getKind, kind)
                .eq(StringUtils.hasText(status), DepositRecord::getStatus, status)
                .orderByDesc(DepositRecord::getCreateTime));
        support.fillCustomer(rows, DepositRecord::getCustomerId, DepositRecord::setCustomerName, DepositRecord::setCustomerPhone);
        return rows;
    }

    @Transactional
    public DepositRecord create(DepositRecord dto) {
        if (dto.getCustomerId() == null || customerMapper.selectById(dto.getCustomerId()) == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择客户");
        }
        if (!KINDS.contains(dto.getKind())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择定金或押金");
        }
        DepositRecord row = new DepositRecord();
        row.setCustomerId(dto.getCustomerId());
        row.setKind(dto.getKind());
        row.setAmount(money(dto.getAmount()));
        row.setStatus(DepositRecord.HELD);
        row.setRemark(trim(dto.getRemark()));
        depositMapper.insert(row);
        return listOne(row.getId());
    }

    @Transactional
    public DepositRecord refund(Long id) {
        return move(id, DepositRecord.REFUNDED);
    }

    @Transactional
    public DepositRecord apply(Long id) {
        return move(id, DepositRecord.APPLIED);
    }

    @Transactional
    public void delete(Long id) {
        DepositRecord row = must(id);
        if (!DepositRecord.HELD.equals(row.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "已退还或已抵扣的记录请保留");
        }
        depositMapper.deleteById(id);
    }

    private DepositRecord move(Long id, String status) {
        DepositRecord row = must(id);
        if (!DepositRecord.HELD.equals(row.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "只有持有中的定金或押金可以处理");
        }
        row.setStatus(status);
        depositMapper.updateById(row);
        return listOne(id);
    }

    private DepositRecord listOne(Long id) {
        DepositRecord row = must(id);
        support.fillCustomer(java.util.List.of(row), DepositRecord::getCustomerId,
                DepositRecord::setCustomerName, DepositRecord::setCustomerPhone);
        return row;
    }

    private DepositRecord must(Long id) {
        DepositRecord row = id == null ? null : depositMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "记录不存在");
        }
        return row;
    }

    private BigDecimal money(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写金额");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
