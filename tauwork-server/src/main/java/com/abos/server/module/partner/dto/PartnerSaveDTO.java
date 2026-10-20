package com.abos.server.module.partner.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PartnerSaveDTO {
    private String name;
    private String phone;
    private Long partnerId;
    private Long customerId;
    private Long productId;
    private BigDecimal amount;
    private Long categoryId;
    private BigDecimal rate;
    private BigDecimal rateL2;
    private Long parentId;
    private String remark;
    private String code;
}
