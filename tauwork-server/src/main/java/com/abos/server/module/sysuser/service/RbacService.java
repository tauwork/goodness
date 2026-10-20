package com.abos.server.module.sysuser.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.community.entity.Community;
import com.abos.server.module.community.mapper.CommunityMapper;
import com.abos.server.module.sysuser.dto.MenuNode;
import com.abos.server.module.sysuser.dto.StaffSaveDTO;
import com.abos.server.module.sysuser.dto.StaffVO;
import com.abos.server.module.sysuser.entity.SysMenu;
import com.abos.server.module.sysuser.entity.SysRole;
import com.abos.server.module.sysuser.entity.SysRoleMenu;
import com.abos.server.module.sysuser.entity.SysUser;
import com.abos.server.module.sysuser.entity.SysUserCommunity;
import com.abos.server.module.sysuser.mapper.SysMenuMapper;
import com.abos.server.module.sysuser.mapper.SysRoleMapper;
import com.abos.server.module.sysuser.mapper.SysRoleMenuMapper;
import com.abos.server.module.sysuser.mapper.SysUserCommunityMapper;
import com.abos.server.module.sysuser.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RbacService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserCommunityMapper userCommunityMapper;
    private final CommunityMapper communityMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<String> permissionsOf(SysUser user) {
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return List.of();
        }
        if ("ADMIN".equals(user.getRoleCode())) {
            return List.of("*");
        }
        SysRole role = roleByCode(user.getRoleCode());
        if (role == null || role.getStatus() == null || role.getStatus() != 1) {
            return List.of();
        }
        List<Long> menuIds = roleMenuIds(role.getId());
        if (menuIds.isEmpty()) {
            return List.of();
        }
        return menuMapper.selectBatchIds(menuIds).stream()
                .map(SysMenu::getPerm)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    public List<MenuNode> menuTreeOf(SysUser user) {
        List<SysMenu> all = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery().orderByAsc(SysMenu::getSortNo).orderByAsc(SysMenu::getId));
        Set<Long> allowed;
        if (user != null && "ADMIN".equals(user.getRoleCode())) {
            allowed = null;
        } else {
            SysRole role = user == null ? null : roleByCode(user.getRoleCode());
            allowed = role == null ? Set.of() : new HashSet<>(roleMenuIds(role.getId()));
        }
        return buildTree(all, 0L, allowed, false);
    }

    public List<MenuNode> fullTree() {
        List<SysMenu> all = menuMapper.selectList(Wrappers.<SysMenu>lambdaQuery().orderByAsc(SysMenu::getSortNo).orderByAsc(SysMenu::getId));
        return buildTree(all, 0L, null, true);
    }

    public List<Long> communityIdsOf(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return userCommunityMapper.selectList(Wrappers.<SysUserCommunity>lambdaQuery()
                        .eq(SysUserCommunity::getUserId, userId))
                .stream().map(SysUserCommunity::getCommunityId).toList();
    }

    public boolean seesAllCommunities(SysUser user) {
        return user != null && "ADMIN".equals(user.getRoleCode());
    }

    public List<SysRole> roles() {
        List<SysRole> list = roleMapper.selectList(Wrappers.<SysRole>lambdaQuery().orderByAsc(SysRole::getId));
        list.forEach(role -> role.setMenuIds(roleMenuIds(role.getId())));
        return list;
    }

    @Transactional
    public SysRole saveRole(SysRole dto, List<Long> menuIds) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写角色名称");
        }
        String code = dto.getCode() == null ? "" : dto.getCode().trim().toUpperCase();
        if (!code.matches("[A-Z][A-Z0-9_]{1,31}")) {
            throw new BizException(ErrorCode.BAD_REQUEST, "角色编码用大写字母开头，只能含大写字母、数字和下划线");
        }
        SysRole same = roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getCode, code));
        if (dto.getId() == null) {
            if (same != null) {
                throw new BizException(ErrorCode.OPERATION_FAILED, "角色编码已存在");
            }
            SysRole role = new SysRole();
            role.setCode(code);
            role.setName(dto.getName().trim());
            role.setRemark(trim(dto.getRemark()));
            role.setStatus(dto.getStatus() != null && dto.getStatus() == 0 ? 0 : 1);
            role.setBuiltin(0);
            roleMapper.insert(role);
            replaceMenus(role.getId(), menuIds);
            role.setMenuIds(roleMenuIds(role.getId()));
            return role;
        }
        SysRole db = roleMapper.selectById(dto.getId());
        if (db == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        if (same != null && !same.getId().equals(db.getId())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "角色编码已存在");
        }
        if (db.getBuiltin() != null && db.getBuiltin() == 1 && !db.getCode().equals(code)) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "内置角色的编码不能修改");
        }
        if ("ADMIN".equals(db.getCode()) && dto.getStatus() != null && dto.getStatus() == 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "系统管理员角色不能停用");
        }
        db.setCode("ADMIN".equals(db.getCode()) ? "ADMIN" : code);
        db.setName(dto.getName().trim());
        db.setRemark(trim(dto.getRemark()));
        if (dto.getStatus() != null) {
            db.setStatus(dto.getStatus() == 0 ? 0 : 1);
        }
        roleMapper.updateById(db);
        replaceMenus(db.getId(), menuIds);
        db.setMenuIds(roleMenuIds(db.getId()));
        return db;
    }

    public void deleteRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        if (role.getBuiltin() != null && role.getBuiltin() == 1) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "内置角色不能删除");
        }
        Long used = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getRoleCode, role.getCode()));
        if (used != null && used > 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "还有账号在使用这个角色");
        }
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, id));
        roleMapper.deleteById(id);
    }

    public List<StaffVO> staff() {
        Map<String, String> roleNames = new HashMap<>();
        roleMapper.selectList(Wrappers.<SysRole>lambdaQuery()).forEach(role -> roleNames.put(role.getCode(), role.getName()));
        Map<Long, String> communityNames = new HashMap<>();
        communityMapper.selectList(Wrappers.<Community>lambdaQuery()).forEach(c -> communityNames.put(c.getId(), c.getName()));
        return userMapper.selectList(Wrappers.<SysUser>lambdaQuery().orderByAsc(SysUser::getId)).stream().map(user -> {
            StaffVO vo = toVo(user, roleNames.get(user.getRoleCode()));
            List<Long> ids = communityIdsOf(user.getId());
            vo.setCommunityIds(ids);
            vo.setCommunityNames(ids.stream().map(communityNames::get).filter(Objects::nonNull).reduce((a, b) -> a + "、" + b).orElse(""));
            return vo;
        }).toList();
    }

    public List<Community> communities() {
        return communityMapper.selectList(Wrappers.<Community>lambdaQuery().orderByAsc(Community::getSortNo));
    }

    @Transactional
    public StaffVO saveStaff(StaffSaveDTO dto, Long operatorId) {
        SysRole role = roleByCode(dto.getRoleCode());
        if (role == null || role.getStatus() == null || role.getStatus() != 1) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择一个启用中的角色");
        }
        if (dto.getId() == null) {
            if (!StringUtils.hasText(dto.getPassword()) || dto.getPassword().length() < 6) {
                throw new BizException(ErrorCode.BAD_REQUEST, "初始密码至少 6 位");
            }
            String username = dto.getUsername().trim();
            Long exists = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, username));
            if (exists != null && exists > 0) {
                throw new BizException(ErrorCode.OPERATION_FAILED, "登录名已存在");
            }
            SysUser user = new SysUser();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
            user.setRealName(dto.getRealName().trim());
            user.setPhone(trim(dto.getPhone()));
            user.setRoleCode(role.getCode());
            user.setStatus(dto.getStatus() != null && dto.getStatus() == 0 ? 0 : 1);
            userMapper.insert(user);
            replaceCommunities(user.getId(), dto.getCommunityIds());
            StaffVO created = toVo(user, role.getName());
            created.setCommunityIds(communityIdsOf(user.getId()));
            return created;
        }
        SysUser db = userMapper.selectById(dto.getId());
        if (db == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "账号不存在");
        }
        if (!db.getUsername().equals(dto.getUsername().trim())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "登录名不能修改");
        }
        guardLastAdmin(db, role.getCode(), dto.getStatus(), operatorId);
        db.setRealName(dto.getRealName().trim());
        db.setPhone(trim(dto.getPhone()));
        db.setRoleCode(role.getCode());
        if (dto.getStatus() != null) {
            db.setStatus(dto.getStatus() == 0 ? 0 : 1);
        }
        if (StringUtils.hasText(dto.getPassword())) {
            if (dto.getPassword().length() < 6) {
                throw new BizException(ErrorCode.BAD_REQUEST, "密码至少 6 位");
            }
            db.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        userMapper.updateById(db);
        replaceCommunities(db.getId(), dto.getCommunityIds());
        StaffVO vo = toVo(db, role.getName());
        vo.setCommunityIds(communityIdsOf(db.getId()));
        return vo;
    }

    private void guardLastAdmin(SysUser db, String nextRole, Integer nextStatus, Long operatorId) {
        boolean leavingAdmin = "ADMIN".equals(db.getRoleCode()) && !"ADMIN".equals(nextRole);
        boolean turnOff = nextStatus != null && nextStatus == 0;
        if (operatorId != null && operatorId.equals(db.getId()) && (leavingAdmin || turnOff)) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "不能停用或改掉自己的管理员角色");
        }
        if ("ADMIN".equals(db.getRoleCode()) && (leavingAdmin || turnOff)) {
            Long admins = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getRoleCode, "ADMIN")
                    .eq(SysUser::getStatus, 1));
            if (admins != null && admins <= 1) {
                throw new BizException(ErrorCode.OPERATION_FAILED, "至少保留一个启用的系统管理员");
            }
        }
    }

    private void replaceMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds == null) {
            return;
        }
        Set<Long> unique = new HashSet<>(menuIds);
        for (Long menuId : unique) {
            if (menuId == null || menuMapper.selectById(menuId) == null) {
                continue;
            }
            SysRoleMenu link = new SysRoleMenu();
            link.setRoleId(roleId);
            link.setMenuId(menuId);
            roleMenuMapper.insert(link);
        }
    }

    private void replaceCommunities(Long userId, List<Long> communityIds) {
        userCommunityMapper.delete(Wrappers.<SysUserCommunity>lambdaQuery().eq(SysUserCommunity::getUserId, userId));
        if (communityIds == null) {
            return;
        }
        for (Long communityId : new HashSet<>(communityIds)) {
            if (communityId == null || communityMapper.selectById(communityId) == null) {
                continue;
            }
            SysUserCommunity link = new SysUserCommunity();
            link.setUserId(userId);
            link.setCommunityId(communityId);
            userCommunityMapper.insert(link);
        }
    }

    private List<Long> roleMenuIds(Long roleId) {
        return roleMenuMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).toList();
    }

    private SysRole roleByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        return roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery().eq(SysRole::getCode, code));
    }

    private List<MenuNode> buildTree(List<SysMenu> all, Long parentId, Set<Long> allowed, boolean includeButtons) {
        List<MenuNode> nodes = new ArrayList<>();
        for (SysMenu menu : all) {
            if (!Objects.equals(parentId, menu.getParentId())) {
                continue;
            }
            if (allowed != null && !allowed.contains(menu.getId())) {
                continue;
            }
            if (!includeButtons && "BUTTON".equals(menu.getMenuType())) {
                continue;
            }
            MenuNode node = new MenuNode();
            node.setId(menu.getId());
            node.setTitle(menu.getTitle());
            node.setPath(menu.getPath());
            node.setIcon(menu.getIcon());
            node.setMenuType(menu.getMenuType());
            node.setPerm(menu.getPerm());
            node.setChildren(buildTree(all, menu.getId(), allowed, includeButtons));
            if ("DIR".equals(menu.getMenuType()) && node.getChildren().isEmpty()) {
                continue;
            }
            nodes.add(node);
        }
        return nodes;
    }

    private StaffVO toVo(SysUser user, String roleName) {
        StaffVO vo = new StaffVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setRoleCode(user.getRoleCode());
        vo.setRoleName(roleName);
        vo.setStatus(user.getStatus());
        vo.setCommunityIds(List.of());
        return vo;
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    public List<String> bookingModes(Collection<Long> communityIds) {
        List<String> modes = new ArrayList<>();
        modes.add("VIDEO");
        if (communityIds == null || communityIds.isEmpty()) {
            return modes;
        }
        communityMapper.selectBatchIds(communityIds).stream()
                .map(Community::getBookingMode)
                .filter(StringUtils::hasText)
                .forEach(modes::add);
        return modes;
    }
}
