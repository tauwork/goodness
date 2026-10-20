package com.abos.server.module.crm.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.crm.entity.CrmFollow;
import com.abos.server.module.crm.service.CrmFollowService;
import com.abos.server.security.LoginContext;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/follow")
@RequiredArgsConstructor
public class CrmFollowAdminController {

    private final CrmFollowService followService;

    @GetMapping("/page")
    public Result<IPage<CrmFollow>> page(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "20") long size,
                                         @RequestParam(required = false) Long customerId,
                                         @RequestParam(required = false) String followType,
                                         @RequestParam(required = false) String keyword) {
        return Result.ok(followService.page(page, size, customerId, followType, keyword));
    }

    @PostMapping
    @SaCheckPermission("crm:manage")
    public Result<CrmFollow> create(@RequestBody CrmFollow follow) {
        return Result.ok(followService.create(follow, LoginContext.currentAdminId()));
    }
}
