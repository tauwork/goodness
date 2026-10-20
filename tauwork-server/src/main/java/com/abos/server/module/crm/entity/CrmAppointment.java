package com.abos.server.module.crm.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_appointment")
public class CrmAppointment extends BaseEntity {

    public static final String PENDING = "PENDING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String DONE = "DONE";
    public static final String CANCELED = "CANCELED";
    public static final String NO_SHOW = "NO_SHOW";

    private Long customerId;
    /** VISIT/STORE/VIDEO/PHONE/MEETING */
    private String visitType;
    private String topic;
    private LocalDate bookDate;
    private String timeSlot;
    private String place;
    private Long ownerId;
    /** PENDING/CONFIRMED/DONE/CANCELED/NO_SHOW */
    private String status;
    private String remark;

    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String customerPhone;
    @TableField(exist = false)
    private String ownerName;
}
