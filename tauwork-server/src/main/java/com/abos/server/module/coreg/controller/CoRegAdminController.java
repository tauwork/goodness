package com.abos.server.module.coreg.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.coreg.dto.CoRegReviewDTO;
import com.abos.server.module.coreg.service.CoRegService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台核对公司注册工单。
 */
@RestController
@RequestMapping("/api/admin/co-reg")
@RequiredArgsConstructor
public class CoRegAdminController {

    private final CoRegService coRegService;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String step) {
        return Result.ok(coRegService.adminList(step));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> one(@PathVariable Long id) {
        return Result.ok(coRegService.adminDetail(id));
    }

    @PostMapping("/{id}/review")
    @SaCheckPermission("workorder:manage")
    public Result<Map<String, Object>> review(@PathVariable Long id, @RequestBody CoRegReviewDTO dto) {
        return Result.ok(coRegService.review(id, dto));
    }
}
