package com.abos.server.security;

import cn.dev33.satoken.stp.StpInterface;
import com.abos.server.module.sysuser.entity.SysUser;
import com.abos.server.module.sysuser.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Sa-Token 权限数据源：按角色返回权限集（与运营平台权限矩阵一致）
 * ADMIN 全量；其他角色按职责分配
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private static final Map<String, List<String>> ROLE_PERMS = Map.of(
            "ADMIN", List.of("*"),
            "DIRECTOR", List.of("product:manage", "cms:manage", "space:manage"),
            "CONSULTANT", List.of(),
            "CASE_OPS", List.of("space:manage"),
            "FINANCE", List.of(),
            "SPACE_OPS", List.of("space:manage", "product:manage")
    );

    private final SysUserMapper sysUserMapper;

    public StpInterfaceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        String id = String.valueOf(loginId);
        if (id.startsWith("C")) {
            return Collections.emptyList();  // 小程序客户无后台权限
        }
        SysUser user = sysUserMapper.selectById(Long.parseLong(id));
        if (user == null) {
            return Collections.emptyList();
        }
        List<String> perms = ROLE_PERMS.get(user.getRoleCode());
        return perms != null ? perms : Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String id = String.valueOf(loginId);
        if (id.startsWith("C")) {
            return Collections.emptyList();
        }
        SysUser user = sysUserMapper.selectById(Long.parseLong(id));
        return user != null ? List.of(user.getRoleCode()) : Collections.emptyList();
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
