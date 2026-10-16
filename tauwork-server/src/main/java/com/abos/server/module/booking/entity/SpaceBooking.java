package com.abos.server.module.booking.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 空间预订单（关联 orders SP 单）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("space_booking")
public class SpaceBooking extends BaseEntity {

    private Long orderId;
    private String orderNo;
    private Long customerId;
    private Long communityId;
    private Long communityProductId;
    private LocalDate bookDate;
    /** HH:mm（HOUR 计价必填） */
    private String startTime;
    private Integer hours;
    /** DAY/MONTH/ONCE 计价数量，默认1 */
    private Integer qty;
    private BigDecimal amount;
    /** PENDING_PAY/PAID/USED/CANCELED */
    private String status;
    private String entryCode;
    private LocalDateTime usedAt;
}
