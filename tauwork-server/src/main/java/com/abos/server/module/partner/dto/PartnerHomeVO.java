package com.abos.server.module.partner.dto;

import com.abos.server.module.partner.entity.PartnerCommission;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PartnerHomeVO {
    private boolean partner;
    private String name;
    private String phone;
    private String code;
    private String sharePath;
    private String joinPath;
    private BigDecimal commissionAmount;
    private BigDecimal directAmount;
    private BigDecimal indirectAmount;
    private BigDecimal otherAmount;
    private BigDecimal withdrawnAmount;
    private BigDecimal pendingAmount;
    private BigDecimal balanceAmount;
    private int customerCount;
    private int directCount;
    private int indirectCount;
    private List<PartnerCustomerVO> customers;
    private List<PartnerCommission> deals;
}
