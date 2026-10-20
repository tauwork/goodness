package com.abos.server.security;

import cn.dev33.satoken.stp.StpInterface;
import com.abos.server.module.sysuser.entity.SysUser;
import com.abos.server.module.sysuser.mapper.SysUserMapper;
import com.abos.server.module.sysuser.service.RbacService;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限数据源：按角色已配置的功能权限返回。系统管理员为全量。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper sysUserMapper;
    private final RbacService rbac;

    public StpInterfaceImpl(SysUserMapper sysUserMapper, RbacService rbac) {
        this.sysUserMapper = sysUserMapper;
        this.rbac = rbac;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        SysUser user = staff(loginId);
        return user == null ? Collections.emptyList() : rbac.permissionsOf(user);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SysUser user = staff(loginId);
        return user != null && user.getRoleCode() != null ? List.of(user.getRoleCode()) : Collections.emptyList();
    }

    private SysUser staff(Object loginId) {
        String id = String.valueOf(loginId);
        if (id.startsWith("C")) {
            return null;
        }
        return sysUserMapper.selectById(Long.parseLong(id));
    }

    /** 角色拥有某权限（ADMIN 通配 *） */
    public static boolean hasPerm(List<String> perms, String perm) {
        return perms.contains("*") || perms.contains(perm);
    }

    /** 供 service 侧查询当前登录角色 */
    public String currentRole(Long adminId) {
        SysUser user = sysUserMapper.selectById(adminId);
        return user != null ? user.getRoleCode() : null;
    }
}
