package com.abos.server.module.lead.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.lead.entity.Lead;
import com.abos.server.module.lead.service.LeadService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台线索池（CRM）：评估/预约自动入库 → 顾问分配
 */
@RestController
@RequestMapping("/api/admin/lead")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @GetMapping("/page")
    public Result<IPage<Lead>> page(@RequestParam(defaultValue = "1") long page,
                                    @RequestParam(defaultValue = "10") long size,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String source) {
        return Result.ok(leadService.page(page, size, status, source));
    }

    @PutMapping("/{id}/assign/{ownerId}")
    @SaCheckPermission("crm:manage")
    public Result<Lead> assign(@PathVariable Long id, @PathVariable Long ownerId) {
        return Result.ok(leadService.assign(id, ownerId));
    }
}
