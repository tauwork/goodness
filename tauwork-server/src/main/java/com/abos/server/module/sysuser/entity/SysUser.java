package com.abos.server.module.sysuser.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;
    private String passwordHash;
    private String realName;
    /** ADMIN/DIRECTOR/CONSULTANT/CASE_OPS/FINANCE/SPACE_OPS */
    private String roleCode;
    private String phone;
    /** 1启用 0停用 */
    private Integer status;
    private LocalDateTime lastLoginAt;
}
