package com.abos.server.module.sysuser.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.INPUT)
    private Long id;
    private Long parentId;
    private String title;
    private String path;
    private String icon;
    private String perm;
    /** DIR / MENU / BUTTON */
    private String menuType;
    private Integer sortNo;
}
