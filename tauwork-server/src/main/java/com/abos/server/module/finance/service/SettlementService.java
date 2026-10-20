package com.abos.server.module.finance.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.finance.entity.RevenueSettlement;
import com.abos.server.module.finance.mapper.RevenueSettlementMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final RevenueSettlementMapper settlementMapper;
    private final CustomerMapper customerMapper;
    private final FinanceSupport support;

    public List<RevenueSettlement> list(String status) {
        List<RevenueSettlement> rows = settlementMapper.selectList(Wrappers.<RevenueSettlement>lambdaQuery()
                .eq(StringUtils.hasText(status), RevenueSettlement::getStatus, status)
                .orderByDesc(RevenueSettlement::getSettleDate)
                .orderByDesc(RevenueSettlement::getCreateTime));
        support.fillCustomer(rows, RevenueSettlement::getCustomerId,
                RevenueSettlement::setCustomerName, RevenueSettlement::setCustomerPhone);
        return rows;
    }

    @Transactional
    public RevenueSettlement create(RevenueSettlement dto) {
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写结算名称");
        }
        if (dto.getCustomerId() != null && customerMapper.selectById(dto.getCustomerId()) == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "客户不存在");
        }
        RevenueSettlement row = new RevenueSettlement();
        row.setCustomerId(dto.getCustomerId());
        row.setTitle(dto.getTitle().trim());
        row.setAmount(money(dto.getAmount()));
        row.setSettleDate(dto.getSettleDate() == null ? LocalDate.now() : dto.getSettleDate());
        row.setStatus(RevenueSettlement.CONFIRMED);
        row.setRemark(trim(dto.getRemark()));
        settlementMapper.insert(row);
        return one(row.getId());
    }

    @Transactional
    public RevenueSettlement voidSettlement(Long id) {
        RevenueSettlement row = must(id);
        if (RevenueSettlement.VOID.equals(row.getStatus())) {
            return one(id);
        }
        row.setStatus(RevenueSettlement.VOID);
        settlementMapper.updateById(row);
        return one(id);
    }

    private RevenueSettlement one(Long id) {
        RevenueSettlement row = must(id);
        support.fillCustomer(List.of(row), RevenueSettlement::getCustomerId,
                RevenueSettlement::setCustomerName, RevenueSettlement::setCustomerPhone);
        return row;
    }

    private RevenueSettlement must(Long id) {
        RevenueSettlement row = id == null ? null : settlementMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "结算记录不存在");
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
