package com.abos.server.module.crm.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_follow")
public class CrmFollow extends BaseEntity {

    private Long customerId;
    private Long leadId;
    private Long appointmentId;
    /** VISIT/MEETING/CALL/WECHAT/OTHER */
    private String followType;
    private String content;
    private LocalDateTime followAt;
    private LocalDateTime nextFollowAt;
    private Long ownerId;

    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String customerPhone;
    @TableField(exist = false)
    private String ownerName;
}
