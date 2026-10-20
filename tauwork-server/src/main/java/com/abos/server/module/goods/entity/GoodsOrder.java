package com.abos.server.module.goods.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 通用商品订单。库存在下单时扣减，取消或退款时退回。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_order")
public class GoodsOrder extends BaseEntity {

    public static final String PENDING_PAY = "PENDING_PAY";
    public static final String PAID = "PAID";
    public static final String DONE = "DONE";
    public static final String CANCELED = "CANCELED";

    private Long orderId;
    private String orderNo;
    private Long customerId;
    private Long productId;
    private String productName;
    private String spec;
    private String coverUrl;
    private Integer qty;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String remark;
    private String status;
}
