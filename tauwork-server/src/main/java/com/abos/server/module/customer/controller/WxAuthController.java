package com.abos.server.module.customer.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.customer.service.WxAuthService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序登录（公开）：POST /api/mini/auth/login {code}
 */
@Validated
@RestController
@RequestMapping("/api/mini/auth")
@RequiredArgsConstructor
public class WxAuthController {

    private final WxAuthService wxAuthService;

    @Data
    public static class WxLoginBody {
        @NotBlank(message = "code不能为空")
        private String code;
    }

    @PostMapping("/login")
    public Result<WxAuthService.LoginVO> login(@Validated @RequestBody WxLoginBody body) {
        return Result.ok(wxAuthService.loginByCode(body.getCode()));
    }
}
