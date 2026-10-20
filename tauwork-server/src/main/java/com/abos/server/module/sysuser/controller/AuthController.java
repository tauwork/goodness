package com.abos.server.module.sysuser.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.abos.server.common.result.Result;
import com.abos.server.module.sysuser.dto.LoginDTO;
import com.abos.server.module.sysuser.service.SysUserService;
import com.abos.server.security.LoginContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    @PostMapping("/login")
    public Result<LoginDTO.LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(sysUserService.login(dto));
    }

    @GetMapping("/me")
    public Result<LoginDTO.LoginVO> me() {
        return Result.ok(sysUserService.profile(LoginContext.currentAdminId(), StpUtil.getTokenValue()));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        sysUserService.logout();
        return Result.ok();
    }
}
