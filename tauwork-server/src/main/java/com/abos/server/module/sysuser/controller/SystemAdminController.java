package com.abos.server.module.sysuser.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.community.entity.Community;
import com.abos.server.module.sysuser.dto.MenuNode;
import com.abos.server.module.sysuser.dto.StaffSaveDTO;
import com.abos.server.module.sysuser.dto.StaffVO;
import com.abos.server.module.sysuser.entity.SysRole;
import com.abos.server.module.sysuser.service.RbacService;
import com.abos.server.security.LoginContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
public class SystemAdminController {

    private final RbacService rbac;

    @GetMapping("/role/list")
    @SaCheckPermission("sys:role")
    public Result<List<SysRole>> roles() {
        return Result.ok(rbac.roles());
    }

    @GetMapping("/role/options")
    @SaCheckPermission("sys:user")
    public Result<List<SysRole>> roleOptions() {
        return Result.ok(rbac.roles().stream().filter(role -> role.getStatus() != null && role.getStatus() == 1).toList());
    }

    @GetMapping("/menu/tree")
    @SaCheckPermission("sys:role")
    public Result<List<MenuNode>> menuTree() {
        return Result.ok(rbac.fullTree());
    }

    @PostMapping("/role")
    @SaCheckPermission("sys:role")
    public Result<SysRole> saveRole(@RequestBody SysRole role) {
        return Result.ok(rbac.saveRole(role, role.getMenuIds()));
    }

    @DeleteMapping("/role/{id}")
    @SaCheckPermission("sys:role")
    public Result<Void> deleteRole(@PathVariable Long id) {
        rbac.deleteRole(id);
        return Result.ok();
    }

    @GetMapping("/user/list")
    @SaCheckPermission("sys:user")
    public Result<List<StaffVO>> users() {
        return Result.ok(rbac.staff());
    }

    @GetMapping("/community/options")
    @SaCheckPermission("sys:user")
    public Result<List<Community>> communities() {
        return Result.ok(rbac.communities());
    }

    @PostMapping("/user")
    @SaCheckPermission("sys:user")
    public Result<StaffVO> saveUser(@Valid @RequestBody StaffSaveDTO dto) {
        return Result.ok(rbac.saveStaff(dto, LoginContext.currentAdminId()));
    }
}
