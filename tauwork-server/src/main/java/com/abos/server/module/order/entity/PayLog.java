package com.abos.server.module.order.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付流水：PREPAY 预支付 / NOTIFY 支付回调 / REFUND 退款
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_log")
public class PayLog extends BaseEntity {

    private String orderNo;
    private String prepayId;
    private String transactionId;
    private String event;
    private Integer success;
    /** 原始报文/参数 JSON */
    private String raw;
}
