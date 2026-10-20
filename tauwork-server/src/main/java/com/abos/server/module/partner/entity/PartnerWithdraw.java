package com.abos.server.module.partner.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("partner_withdraw")
public class PartnerWithdraw extends BaseEntity {

    public static final String PENDING = "PENDING";
    public static final String PAID = "PAID";
    public static final String REJECTED = "REJECTED";

    private Long partnerId;
    private BigDecimal amount;
    private String status;
    private String remark;
}
