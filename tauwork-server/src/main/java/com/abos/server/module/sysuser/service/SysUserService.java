package com.abos.server.module.sysuser.service;

import cn.dev33.satoken.stp.StpUtil;
import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.sysuser.dto.LoginDTO;
import com.abos.server.module.sysuser.entity.SysRole;
import com.abos.server.module.sysuser.entity.SysUser;
import com.abos.server.module.sysuser.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper sysUserMapper;
    private final RbacService rbac;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginDTO.LoginVO login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        StpUtil.login(user.getId());
        user.setLastLoginAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
        return profile(user.getId(), StpUtil.getTokenValue());
    }

    public LoginDTO.LoginVO profile(Long userId, String token) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "账号不存在");
        }
        LoginDTO.LoginVO vo = new LoginDTO.LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setRealName(user.getRealName());
        vo.setRoleCode(user.getRoleCode());
        SysRole role = null;
        vo.setPerms(rbac.permissionsOf(user));
        vo.setMenus(rbac.menuTreeOf(user));
        vo.setCommunityIds(rbac.seesAllCommunities(user) ? List.of() : rbac.communityIdsOf(user.getId()));
        for (var item : rbac.roles()) {
            if (item.getCode().equals(user.getRoleCode())) {
                role = item;
            }
        }
        vo.setRoleName(role == null ? user.getRoleCode() : role.getName());
        return vo;
    }

    public void logout() {
        StpUtil.logout();
    }

    public List<StaffOption> options() {
        return sysUserMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                        .select(SysUser::getId, SysUser::getRealName, SysUser::getUsername, SysUser::getRoleCode)
                        .eq(SysUser::getStatus, 1)
                        .orderByAsc(SysUser::getId))
                .stream()
                .map(u -> new StaffOption(u.getId(), display(u), u.getRoleCode()))
                .toList();
    }

    public Map<Long, String> names(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        return sysUserMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                        .select(SysUser::getId, SysUser::getRealName, SysUser::getUsername)
                        .in(SysUser::getId, ids))
                .stream()
                .collect(Collectors.toMap(SysUser::getId, this::display, (a, b) -> a));
    }

    private String display(SysUser user) {
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName();
        }
        return user.getUsername();
    }

    public record StaffOption(Long id, String realName, String roleCode) {}
}
