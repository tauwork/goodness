package com.abos.server.module.svcorder.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.abos.server.common.result.Result;
import com.abos.server.module.svcorder.entity.SvcOrder;
import com.abos.server.module.svcorder.service.SvcOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台服务订单：确认对公转账到账。
 */
@RestController
@RequestMapping("/api/admin/svc-order")
@RequiredArgsConstructor
public class SvcOrderAdminController {

    private final SvcOrderService svcOrderService;

    @GetMapping("/list")
    public Result<List<SvcOrder>> list(@RequestParam(required = false) String status) {
        return Result.ok(svcOrderService.adminList(status));
    }

    @PutMapping("/{id}/confirm")
    @SaCheckPermission(value = {"order:manage", "finance:manage"}, mode = SaMode.OR)
    public Result<SvcOrder> confirm(@PathVariable Long id) {
        return Result.ok(svcOrderService.confirm(id));
    }
}
