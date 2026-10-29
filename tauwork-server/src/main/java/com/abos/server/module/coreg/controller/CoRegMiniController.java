package com.abos.server.module.coreg.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.coreg.dto.CoRegKycDTO;
import com.abos.server.module.coreg.dto.CoRegNameDTO;
import com.abos.server.module.coreg.dto.CoRegProfileDTO;
import com.abos.server.module.coreg.service.CoRegService;
import com.abos.server.security.LoginContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 小程序我的业务：公司注册工单进度与资料提交。
 */
@RestController
@RequestMapping("/api/mini/co-reg")
@RequiredArgsConstructor
public class CoRegMiniController {

    private final CoRegService coRegService;

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> my() {
        return Result.ok(coRegService.my(LoginContext.currentCustomerId()));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> one(@PathVariable Long id) {
        return Result.ok(coRegService.detail(LoginContext.currentCustomerId(), id));
    }

    @PutMapping("/{id}/name")
    public Result<Map<String, Object>> name(@PathVariable Long id, @RequestBody CoRegNameDTO dto) {
        return Result.ok(coRegService.submitName(LoginContext.currentCustomerId(), id, dto));
    }

    @PutMapping("/{id}/profile")
    public Result<Map<String, Object>> profile(@PathVariable Long id, @RequestBody CoRegProfileDTO dto) {
        return Result.ok(coRegService.submitProfile(LoginContext.currentCustomerId(), id, dto));
    }

    @PutMapping("/{id}/kyc")
    public Result<Map<String, Object>> kyc(@PathVariable Long id, @RequestBody CoRegKycDTO dto) {
        return Result.ok(coRegService.submitKyc(LoginContext.currentCustomerId(), id, dto));
    }

    @PostMapping("/{id}/sign")
    public Result<Map<String, Object>> sign(@PathVariable Long id) {
        return Result.ok(coRegService.confirmSign(LoginContext.currentCustomerId(), id));
    }
}
