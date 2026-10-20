package com.abos.server.module.team.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consultant")
public class Consultant extends BaseEntity {

    private String name;
    private String avatarUrl;
    /** 头衔 */
    private String title;
    /** 价值服务 */
    private String valueService;
    /** 性格特点 */
    private String personality;
    private Long communityId;

    @TableField(exist = false)
    private String communityName;
}
