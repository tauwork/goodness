package com.abos.server.module.cms.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.cms.entity.CmsContent;
import com.abos.server.module.cms.service.CmsContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端内容展示（公开只读，放行见 SaTokenConfig）：
 *  GET /api/mini/cms/banner   首页广告轮播帧
 *  GET /api/mini/cms/news     公告资讯列表
 *  GET /api/mini/cms/news/{id} 资讯详情
 *  GET /api/mini/cms/story    首页客户故事
 */
@RestController
@RequestMapping("/api/mini/cms")
@RequiredArgsConstructor
public class CmsMiniController {

    private final CmsContentService cmsService;

    @GetMapping("/banner")
    public Result<List<CmsContent>> banners() {
        return Result.ok(cmsService.listBanners());
    }

    @GetMapping("/story")
    public Result<List<CmsContent>> stories() {
        return Result.ok(cmsService.listStories());
    }

    @GetMapping("/news")
    public Result<List<CmsContent>> news(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "10") long size) {
        return Result.ok(cmsService.listNews(page, size));
    }

    @GetMapping("/news/{id}")
    public Result<CmsContent> newsDetail(@PathVariable Long id) {
        return Result.ok(cmsService.newsDetail(id));
    }
}
