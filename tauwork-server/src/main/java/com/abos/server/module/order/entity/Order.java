package com.abos.server.module.order.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Order extends BaseEntity {

    private String orderNo;
    /** SERVICE/SPACE/LEASE */
    private String type;
    private Long customerId;
    private String openid;
    private Long productId;
    private String productName;
    private BigDecimal amount;
    /** UNPAID/PAID/INSTALLMENT/REFUNDED */
    private String payStatus;
    /** OFFLINE/WECHAT/AUTO_DEBIT */
    private String payChannel;
    private Long caseId;
    private Long distRefId;
}
