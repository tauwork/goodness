package com.abos.server.module.sysuser.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.sysuser.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
public class StaffAdminController {

    private final SysUserService sysUserService;

    @GetMapping
    public Result<List<SysUserService.StaffOption>> options() {
        return Result.ok(sysUserService.options());
    }
}
