package com.abos.server.module.customer.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class Customer extends BaseEntity {

    private String wxOpenid;
    private String name;
    private String phone;
    private String phoneHash;
    private Long ownerId;
    /** NEW/ASSESSING/APPOINTED/SIGNED/RENEWING/LOST */
    private String stage;
    private String source;
    private String familyNote;
}
