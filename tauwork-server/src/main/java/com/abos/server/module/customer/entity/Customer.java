package com.abos.server.module.customer.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class Customer extends BaseEntity {

    public static final String STAGE_POOL = "POOL";
    public static final String STAGE_INTENT = "INTENT";
    public static final String STAGE_SIGNED = "SIGNED";
    public static final String STAGE_REPEAT = "REPEAT";
    public static final String STAGE_BLACKLIST = "BLACKLIST";

    private String wxOpenid;
    private String name;
    private String phone;
    private String phoneHash;
    /** M/F */
    private String gender;
    private Long ownerId;
    /** POOL公海/INTENT意向/SIGNED签约/REPEAT复购/BLACKLIST黑名单 */
    private String stage;
    private String source;
    /** 归属合伙人 */
    private Long partnerId;
    private LocalDateTime partnerBindAt;
    /** LINK分享 / ADMIN后台指定 */
    private String partnerBindSource;
    private String city;
    private String wechat;
    private String company;
    private String address;
    private String familyNote;
    private String remark;
    private LocalDateTime nextFollowAt;

    @TableField(exist = false)
    private String ownerName;
}
