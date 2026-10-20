package com.abos.server.module.booking.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 服务咨询预约（小程序提交 → 后台确认 → 完成/取消归因）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("svc_booking")
public class SvcBooking extends BaseEntity {

    private Long customerId;
    private Long productId;
    private String productName;
    private String topic;
    /** VIDEO/FUTIAN/BAOAN/KWAI_TSING */
    private String mode;
    private LocalDate bookDate;
    /** HH:mm */
    private String timeSlot;
    /** PENDING/CONFIRMED/DONE/CANCELED/NO_SHOW */
    private String status;
    private String remark;
    /** 未转化归因 */
    private String failReason;
    private Long consultantId;

    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String customerPhone;
    @TableField(exist = false)
    private String consultantName;
}
