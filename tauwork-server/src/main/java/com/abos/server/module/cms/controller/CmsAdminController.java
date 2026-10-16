package com.abos.server.module.cms.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.cms.entity.CmsContent;
import com.abos.server.module.cms.service.CmsContentService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台内容维护：三类内容（BANNER 轮播 / NEWS 公告资讯 / CASE_STORY 客户故事）
 * 对应运营平台原型「产品管理-广告轮播」与「内容运营 CMS」两页的数据源
 */
@RestController
@RequestMapping("/api/admin/cms")
@RequiredArgsConstructor
public class CmsAdminController {

    private final CmsContentService cmsService;

    @GetMapping("/page")
    @SaCheckPermission("cms:manage")
    public Result<IPage<CmsContent>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String contentType,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String keyword) {
        return Result.ok(cmsService.adminPage(page, size, contentType, status, keyword));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("cms:manage")
    public Result<CmsContent> detail(@PathVariable Long id) {
        return Result.ok(cmsService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("cms:manage")
    public Result<CmsContent> create(@RequestBody CmsContent content) {
        return Result.ok(cmsService.create(content));
    }

    @PutMapping
    @SaCheckPermission("cms:manage")
    public Result<CmsContent> update(@RequestBody CmsContent content) {
        return Result.ok(cmsService.update(content));
    }

    /** status: DRAFT / PUBLISHED / OFFLINE */
    @PutMapping("/{id}/status/{status}")
    @SaCheckPermission("cms:manage")
    public Result<CmsContent> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return Result.ok(cmsService.changeStatus(id, status));
    }

    /** 删除（已上架内容须先下架） */
    @DeleteMapping("/{id}")
    @SaCheckPermission("cms:manage")
    public Result<Void> delete(@PathVariable Long id) {
        cmsService.delete(id);
        return Result.ok();
    }

    /** 置顶 / 取消置顶 */
    @PutMapping("/{id}/top")
    @SaCheckPermission("cms:manage")
    public Result<CmsContent> toggleTop(@PathVariable Long id) {
        return Result.ok(cmsService.toggleTop(id));
    }
}
