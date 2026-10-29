package com.abos.server.module.svcorder.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 服务购买订单。不扣库存，支付成功或转账凭证确认后视为已支付。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("svc_order")
public class SvcOrder extends BaseEntity {

    public static final String WECHAT = "WECHAT";
    public static final String TRANSFER = "TRANSFER";

    public static final String PENDING_PAY = "PENDING_PAY";
    public static final String CONFIRMING = "CONFIRMING";
    public static final String PAID = "PAID";
    public static final String CANCELED = "CANCELED";

    private Long orderId;
    private String orderNo;
    private Long customerId;
    private Long productId;
    private String productName;
    private String coverUrl;
    private BigDecimal amount;
    private String payChannel;
    private String status;
    private String voucherUrl;
}
