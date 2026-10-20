package com.abos.server.module.community.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.community.dto.CommunityCardVO;
import com.abos.server.module.community.dto.CommunityProductVO;
import com.abos.server.module.community.entity.Community;
import com.abos.server.module.community.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序「空间栏目」（公开只读）：
 *  GET /api/mini/space/community        社区店列表（可按城市筛选）
 *  GET /api/mini/space/community/{id}   社区店详情
 *  GET /api/mini/space/community/{id}/product  店内可售空间产品（WeWork 式场地列表）
 */
@RestController
@RequestMapping("/api/mini/space")
@RequiredArgsConstructor
public class CommunityMiniController {

    private final CommunityService communityService;

    @GetMapping("/community")
    public Result<List<Community>> communityList(@RequestParam(required = false) String city) {
        return Result.ok(communityService.miniList(city));
    }

    @GetMapping("/community-card")
    public Result<List<CommunityCardVO>> cards(@RequestParam(required = false) Double lat,
                                               @RequestParam(required = false) Double lng,
                                               @RequestParam(required = false) String keyword) {
        return Result.ok(communityService.miniCards(lat, lng, keyword));
    }

    @GetMapping("/community/{id}")
    public Result<Community> communityDetail(@PathVariable Long id) {
        return Result.ok(communityService.miniGet(id));
    }

    @GetMapping("/community/{id}/product")
    public Result<List<CommunityProductVO>> communityProducts(@PathVariable Long id) {
        return Result.ok(communityService.miniCommunityProducts(id));
    }
}
