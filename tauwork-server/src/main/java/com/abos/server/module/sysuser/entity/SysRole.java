package com.abos.server.module.sysuser.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private String code;
    private String name;
    private String remark;
    /** 1启用 0停用 */
    private Integer status;
    /** 1内置 */
    private Integer builtin;

    @TableField(exist = false)
    private List<Long> menuIds;
}
