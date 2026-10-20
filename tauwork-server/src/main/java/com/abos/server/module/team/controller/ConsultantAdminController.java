package com.abos.server.module.team.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.team.entity.Consultant;
import com.abos.server.module.team.service.ConsultantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/consultant")
@RequiredArgsConstructor
public class ConsultantAdminController {

    private final ConsultantService consultantService;

    @GetMapping("/list")
    public Result<List<Consultant>> list(@RequestParam(required = false) Long communityId) {
        return Result.ok(consultantService.list(communityId));
    }

    @PostMapping
    @SaCheckPermission("team:manage")
    public Result<Consultant> create(@RequestBody Consultant consultant) {
        return Result.ok(consultantService.create(consultant));
    }

    @PutMapping
    @SaCheckPermission("team:manage")
    public Result<Consultant> update(@RequestBody Consultant consultant) {
        return Result.ok(consultantService.update(consultant));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("team:manage")
    public Result<Void> delete(@PathVariable Long id) {
        consultantService.delete(id);
        return Result.ok();
    }
}
