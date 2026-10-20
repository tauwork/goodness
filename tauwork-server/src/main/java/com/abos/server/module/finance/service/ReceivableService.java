package com.abos.server.module.finance.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.finance.entity.ReceivableBill;
import com.abos.server.module.finance.mapper.ReceivableBillMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceivableService {

    private final ReceivableBillMapper billMapper;
    private final CustomerMapper customerMapper;
    private final FinanceSupport support;

    public List<ReceivableBill> list(String status) {
        List<ReceivableBill> rows = billMapper.selectList(Wrappers.<ReceivableBill>lambdaQuery()
                .eq(StringUtils.hasText(status), ReceivableBill::getStatus, status)
                .orderByDesc(ReceivableBill::getCreateTime));
        support.fillCustomer(rows, ReceivableBill::getCustomerId, ReceivableBill::setCustomerName, ReceivableBill::setCustomerPhone);
        return rows;
    }

    @Transactional
    public ReceivableBill create(ReceivableBill dto) {
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写账单名称");
        }
        if (dto.getCustomerId() == null || customerMapper.selectById(dto.getCustomerId()) == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择客户");
        }
        ReceivableBill row = new ReceivableBill();
        row.setCustomerId(dto.getCustomerId());
        row.setTitle(dto.getTitle().trim());
        row.setAmount(money(dto.getAmount()));
        row.setReceived(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        row.setStatus(ReceivableBill.OPEN);
        row.setDueDate(dto.getDueDate());
        row.setRemark(trim(dto.getRemark()));
        billMapper.insert(row);
        return one(row.getId());
    }

    @Transactional
    public ReceivableBill receive(Long id, BigDecimal amount) {
        ReceivableBill row = must(id);
        if (ReceivableBill.VOID.equals(row.getStatus()) || ReceivableBill.SETTLED.equals(row.getStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "这份账单不能再收款");
        }
        BigDecimal pay = money(amount);
        BigDecimal next = row.getReceived().add(pay);
        if (next.compareTo(row.getAmount()) > 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "收款金额超过未收余额");
        }
        row.setReceived(next);
        row.setStatus(next.compareTo(row.getAmount()) == 0 ? ReceivableBill.SETTLED : ReceivableBill.PARTIAL);
        billMapper.updateById(row);
        return one(id);
    }

    @Transactional
    public ReceivableBill voidBill(Long id) {
        ReceivableBill row = must(id);
        if (row.getReceived() != null && row.getReceived().signum() > 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "已有收款，不能作废");
        }
        row.setStatus(ReceivableBill.VOID);
        billMapper.updateById(row);
        return one(id);
    }

    @Transactional
    public void delete(Long id) {
        ReceivableBill row = must(id);
        if (row.getReceived() != null && row.getReceived().signum() > 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "已有收款的账单请保留");
        }
        billMapper.deleteById(id);
    }

    private ReceivableBill one(Long id) {
        ReceivableBill row = must(id);
        support.fillCustomer(List.of(row), ReceivableBill::getCustomerId,
                ReceivableBill::setCustomerName, ReceivableBill::setCustomerPhone);
        return row;
    }

    private ReceivableBill must(Long id) {
        ReceivableBill row = id == null ? null : billMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "账单不存在");
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
