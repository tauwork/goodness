package com.abos.server.module.cms.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 内容（三类统一建模）：
 *  BANNER      小程序首页广告轮播帧
 *  NEWS        公告资讯
 *  CASE_STORY  客户案例故事
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cms_content")
public class CmsContent extends BaseEntity {

    public static final String TYPE_BANNER = "BANNER";
    public static final String TYPE_NEWS = "NEWS";
    public static final String TYPE_CASE_STORY = "CASE_STORY";

    private String contentType;
    private String title;
    private String subtitle;
    /** 正文（NEWS 用） */
    private String body;
    /** 角标文案 */
    private String badge;
    /** 样式色 NAVY/GOLD/PURPLE/TEAL */
    private String coverStyle;
    private String coverUrl;
    /** ASSESS/SVC/NEWS/ACTIVITY/NONE */
    private String linkType;
    private String linkTarget;
    /** HOME_BANNER/NEWS_LIST/HOME_STORY */
    private String scope;
    /** DRAFT/PUBLISHED/OFFLINE */
    private String status;
    /** 置顶 1/0 */
    private Integer isTop;
    private LocalDateTime effectiveAt;
    private LocalDateTime expireAt;
    private Integer sortNo;
    /** 扩展 JSON：故事身份/服务组合等 */
    private String meta;

    public boolean isPublishedNow() {
        if (!"PUBLISHED".equals(status)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return (effectiveAt == null || !effectiveAt.isAfter(now))
                && (expireAt == null || expireAt.isAfter(now));
    }
}
