package com.abos.server.module.cms.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.cms.entity.CmsContent;
import com.abos.server.module.cms.mapper.CmsContentMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CmsContentService {

    private final CmsContentMapper cmsMapper;

    private static final Set<String> VALID_TYPES = Set.of(
            CmsContent.TYPE_BANNER, CmsContent.TYPE_NEWS, CmsContent.TYPE_CASE_STORY);

    /* ---------------- 后台维护 ---------------- */

    /** 后台分页：置顶优先，其余按更新时间降序（最新在前） */
    public IPage<CmsContent> adminPage(long page, long size, String contentType, String status, String keyword) {
        return cmsMapper.selectPage(new Page<>(page, size),
                Wrappers.<CmsContent>lambdaQuery()
                        .eq(StringUtils.hasText(contentType), CmsContent::getContentType, contentType)
                        .eq(StringUtils.hasText(status), CmsContent::getStatus, status)
                        .like(StringUtils.hasText(keyword), CmsContent::getTitle, keyword)
                        .orderByDesc(CmsContent::getIsTop)
                        .orderByDesc(CmsContent::getUpdateTime));
    }

    public CmsContent create(CmsContent content) {
        validateType(content.getContentType());
        content.setId(null);
        if (!StringUtils.hasText(content.getStatus())) {
            content.setStatus("DRAFT");
        }
        if (content.getSortNo() == null) {
            content.setSortNo(0);
        }
        cmsMapper.insert(content);
        return content;
    }

    public CmsContent update(CmsContent content) {
        if (content.getId() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "id不能为空");
        }
        CmsContent db = cmsMapper.selectById(content.getId());
        if (db == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "内容不存在");
        }
        validateType(content.getContentType());
        cmsMapper.updateById(content);
        return cmsMapper.selectById(content.getId());
    }

    /** 发布 / 下架 / 退回草稿 */
    public CmsContent changeStatus(Long id, String status) {
        CmsContent db = getById(id);
        if (!Set.of("DRAFT", "PUBLISHED", "OFFLINE").contains(status)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "非法状态: " + status);
        }
        db.setStatus(status);
        cmsMapper.updateById(db);
        return db;
    }

    /** 删除：已上架（PUBLISHED）不允许删除，须先下架 */
    public void delete(Long id) {
        CmsContent db = getById(id);
        if ("PUBLISHED".equals(db.getStatus())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该内容已上架，请先下架再删除");
        }
        cmsMapper.deleteById(id);
    }

    /** 置顶 / 取消置顶（置顶内容在后台与小程序端均排最前） */
    public CmsContent toggleTop(Long id) {
        CmsContent db = getById(id);
        db.setIsTop(db.getIsTop() != null && db.getIsTop() == 1 ? 0 : 1);
        cmsMapper.updateById(db);
        return db;
    }

    public CmsContent getById(Long id) {
        CmsContent c = cmsMapper.selectById(id);
        if (c == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "内容不存在");
        }
        return c;
    }

    private void validateType(String type) {
        if (!VALID_TYPES.contains(type)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "内容类型必须是 BANNER/NEWS/CASE_STORY");
        }
    }

    /* ---------------- 小程序端展示 ---------------- */

    /** 首页轮播帧：已发布且在生效期内的 BANNER，按帧位排序（到期帧自动排除） */
    public List<CmsContent> listBanners() {
        return listPublished(CmsContent.TYPE_BANNER, "HOME_BANNER");
    }

    /** 首页客户故事 */
    public List<CmsContent> listStories() {
        return listPublished(CmsContent.TYPE_CASE_STORY, "HOME_STORY");
    }

    /** 公告资讯列表：置顶、排序在前，再按展示日期 */
    public List<CmsContent> listNews(long page, long size) {
        IPage<CmsContent> p = cmsMapper.selectPage(new Page<>(page, size),
                Wrappers.<CmsContent>lambdaQuery()
                        .eq(CmsContent::getContentType, CmsContent.TYPE_NEWS)
                        .eq(CmsContent::getStatus, "PUBLISHED")
                        .orderByDesc(CmsContent::getIsTop)
                        .orderByDesc(CmsContent::getSortNo)
                        .orderByDesc(CmsContent::getEffectiveAt));
        return p.getRecords().stream().filter(CmsContent::isPublishedNow).toList();
    }

    /** 资讯详情（仅返回已发布的） */
    public CmsContent newsDetail(Long id) {
        return publishedDetail(id, CmsContent.TYPE_NEWS, "资讯不存在或已下架");
    }

    /** 客户故事详情（仅返回已发布的） */
    public CmsContent storyDetail(Long id) {
        return publishedDetail(id, CmsContent.TYPE_CASE_STORY, "故事不存在或已下架");
    }

    private CmsContent publishedDetail(Long id, String type, String missing) {
        CmsContent c = getById(id);
        if (!type.equals(c.getContentType()) || !c.isPublishedNow()) {
            throw new BizException(ErrorCode.NOT_FOUND, missing);
        }
        return c;
    }

    private List<CmsContent> listPublished(String type, String scope) {
        return cmsMapper.selectList(Wrappers.<CmsContent>lambdaQuery()
                        .eq(CmsContent::getContentType, type)
                        .eq(CmsContent::getScope, scope)
                        .eq(CmsContent::getStatus, "PUBLISHED")
                        .orderByDesc(CmsContent::getIsTop)
                        .orderByDesc(CmsContent::getSortNo))
                .stream()
                .filter(CmsContent::isPublishedNow)
                .toList();
    }

    /* ---------------- 定时任务：到期内容自动下架 ---------------- */

    @Scheduled(fixedDelay = 300_000)
    public void offlineExpired() {
        List<CmsContent> expired = cmsMapper.selectList(Wrappers.<CmsContent>lambdaQuery()
                .eq(CmsContent::getStatus, "PUBLISHED")
                .isNotNull(CmsContent::getExpireAt)
                .le(CmsContent::getExpireAt, java.time.LocalDateTime.now()));
        expired.forEach(c -> {
            c.setStatus("OFFLINE");
            cmsMapper.updateById(c);
        });
    }
}
