package com.abos.server.module.sysuser.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user_community")
public class SysUserCommunity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private Long communityId;
}
