package com.abos.server.module.partner.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("partner_commission")
public class PartnerCommission extends BaseEntity {

    public static final String ACCRUED = "ACCRUED";
    public static final String REVERSED = "REVERSED";
    public static final String SOURCE_ORDER = "ORDER";
    public static final String SOURCE_MANUAL = "MANUAL";

    private Long partnerId;
    private Long customerId;
    private Long orderId;
    private String orderNo;
    private Long productId;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal orderAmount;
    private BigDecimal rate;
    private BigDecimal commissionAmount;
    private String status;
    private String source;

    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String customerPhone;
}
