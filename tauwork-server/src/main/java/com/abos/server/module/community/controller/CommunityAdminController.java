package com.abos.server.module.community.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.community.entity.Community;
import com.abos.server.module.community.entity.CommunityProduct;
import com.abos.server.module.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台：社区店管理 + 社区×产品关联（对应运营平台「空间运营-网点与场地」）
 */
@RestController
@RequestMapping("/api/admin/community")
@RequiredArgsConstructor
public class CommunityAdminController {

    private final CommunityService communityService;

    /* ---------- 社区店 ---------- */

    @GetMapping("/list")
    public Result<List<Community>> list(@RequestParam(required = false) String city,
                                        @RequestParam(required = false) String status) {
        return Result.ok(communityService.adminList(city, status));
    }

    @GetMapping("/{id}")
    public Result<Community> detail(@PathVariable Long id) {
        return Result.ok(communityService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("space:manage")
    public Result<Community> create(@RequestBody Community community) {
        return Result.ok(communityService.create(community));
    }

    @PutMapping
    @SaCheckPermission("space:manage")
    public Result<Community> update(@RequestBody Community community) {
        return Result.ok(communityService.update(community));
    }

    @PutMapping("/{id}/on")
    @SaCheckPermission("space:manage")
    public Result<Community> onSale(@PathVariable Long id) {
        return Result.ok(communityService.setOnSale(id, true));
    }

    @PutMapping("/{id}/off")
    @SaCheckPermission("space:manage")
    public Result<Community> offSale(@PathVariable Long id) {
        return Result.ok(communityService.setOnSale(id, false));
    }

    /* ---------- 社区×产品关联 ---------- */

    @GetMapping("/{id}/product")
    public Result<List<CommunityProduct>> listBindings(@PathVariable Long id) {
        return Result.ok(communityService.listBindings(id));
    }

    /** 基于社区店添加产品关联（社区级价格/库存/单位可覆盖产品默认值） */
    @PostMapping("/bind-product")
    @SaCheckPermission("space:manage")
    public Result<CommunityProduct> bindProduct(@RequestBody CommunityProduct cp) {
        return Result.ok(communityService.bindProduct(cp));
    }

    @DeleteMapping("/bind-product/{bindId}")
    @SaCheckPermission("space:manage")
    public Result<Void> unbindProduct(@PathVariable Long bindId) {
        communityService.unbindProduct(bindId);
        return Result.ok();
    }
}
