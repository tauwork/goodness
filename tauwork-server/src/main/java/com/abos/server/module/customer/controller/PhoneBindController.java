package com.abos.server.module.customer.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.customer.service.PhoneBindService;
import com.abos.server.security.LoginContext;
import jakarta.validation.constraints.NotBlank;
import cn.dev33.satoken.stp.StpUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小程序手机号绑定（需登录）：
 *  POST /api/mini/phone/send-code {phone}
 *  POST /api/mini/phone/bind {phone, code}
 */
@Validated
@RestController
@RequestMapping("/api/mini/phone")
@RequiredArgsConstructor
public class PhoneBindController {

    private final PhoneBindService phoneBindService;
    private final CustomerMapper customerMapper;

    @Data
    public static class PhoneBody {
        @NotBlank(message = "手机号不能为空")
        private String phone;
        private String code;
    }

    @PostMapping("/send-code")
    public Result<Void> sendCode(@Validated @RequestBody PhoneBody body) {
        LoginContext.currentCustomerId();
        phoneBindService.sendCode(body.getPhone());
        return Result.ok();
    }

    @PostMapping("/bind")
    public Result<Map<String, Object>> bind(@Validated @RequestBody PhoneBody body) {
        Customer customer = phoneBindService.bind(LoginContext.currentCustomerId(), body.getPhone(), body.getCode());
        return Result.ok(Map.of(
                "customerId", customer.getId(),
                "token", StpUtil.getTokenValue(),
                "phone", customer.getPhone()));
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> status() {
        Customer c = customerMapper.selectById(LoginContext.currentCustomerId());
        String phone = c.getPhone();
        return Result.ok(Map.of(
                "bound", phone != null,
                "masked", phone == null ? null : phone.substring(0, 3) + "****" + phone.substring(7)));
    }
}
