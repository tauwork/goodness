package com.abos.server.module.lead.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`lead`")
public class Lead extends BaseEntity {

    private Long customerId;
    private String name;
    private String phone;
    /** ASSESS/BOOKING/CONTENT/REFERRAL/EVENT/RENEWAL_CAL */
    private String source;
    private String intent;
    private Integer assessScore;
    /** NEW/ASSIGNED/IN_PROGRESS/CONVERTED/PUBLIC_POOL */
    private String status;
    private Long ownerId;
    private LocalDateTime assignedAt;
    private LocalDateTime lastFollowAt;
}
