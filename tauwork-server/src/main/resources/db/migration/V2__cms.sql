-- ============================================================
-- V0.2 内容管理模块：三类内容统一建模一张表（BANNER/NEWS/CASE_STORY）
-- 覆盖：小程序首页广告轮播、公告资讯、客户故事展示
-- ============================================================

CREATE TABLE cms_content (
    id            BIGINT       NOT NULL,
    content_type  VARCHAR(16)  NOT NULL COMMENT 'BANNER轮播广告/NEWS公告资讯/CASE_STORY客户故事',
    title         VARCHAR(128) NOT NULL COMMENT '标题（轮播主题/资讯标题/故事引语）',
    subtitle      VARCHAR(255) DEFAULT NULL COMMENT '副标题/摘要',
    body          MEDIUMTEXT   DEFAULT NULL COMMENT '正文（NEWS 用，支持富文本/Markdown）',
    badge         VARCHAR(64)  DEFAULT NULL COMMENT '角标文案，如 3分钟·免费 / 10/17·深圳 / 4周获批',
    cover_style   VARCHAR(32)  DEFAULT NULL COMMENT '样式色 NAVY/GOLD/PURPLE/TEAL（原型色板）',
    cover_url     VARCHAR(255) DEFAULT NULL COMMENT '封面图（有图时优先于样式色）',
    link_type     VARCHAR(32)  DEFAULT NULL COMMENT '跳转类型: ASSESS评估/SVC服务/NEWS资讯/ACTIVITY活动/NONE',
    link_target   VARCHAR(64)  DEFAULT NULL COMMENT '跳转目标（svc_product.id / cms_content.id / 活动页）',
    scope         VARCHAR(32)  NOT NULL DEFAULT 'HOME' COMMENT '展示位: HOME_BANNER/NEWS_LIST/HOME_STORY',
    status        VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT草稿/PUBLISHED发布/OFFLINE下架',
    effective_at  DATETIME     DEFAULT NULL COMMENT '生效时间（定时上架，空=立即）',
    expire_at     DATETIME     DEFAULT NULL COMMENT '失效时间（到期自动下架，空=长期）',
    sort_no       INT          NOT NULL DEFAULT 0 COMMENT '排序（轮播帧位/置顶）',
    meta          JSON         DEFAULT NULL COMMENT '扩展字段（故事主角身份/服务组合/成果标签等）',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_type_status (content_type, status, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容（轮播/资讯/案例）';

-- 种子数据：与小程序原型 v1.1 首页一致的三帧轮播 + 三条资讯 + 四个故事
INSERT INTO cms_content (id, content_type, title, subtitle, badge, cover_style, link_type, link_target, scope, status, sort_no) VALUES
(1001,'BANNER','香港身份智能评估','高才 / 优才 / 专才 / 港硕 · 一键匹配适配路径','3分钟 · 免费','NAVY','ASSESS',NULL,'HOME_BANNER','PUBLISHED',1),
(1002,'BANNER','香港公司注册 + 银行开户','注册 · 秘书 · 开户 · 审计一站式代办，续签创业两不误','限时套餐','GOLD','SVC','3','HOME_BANNER','PUBLISHED',2),
(1003,'BANNER','深港双城线下沙龙','家庭身份与教育规划策略 · 剩余名额 22 个','10/17 · 深圳','PURPLE','ACTIVITY',NULL,'HOME_BANNER','PUBLISHED',3);

INSERT INTO cms_content (id, content_type, title, subtitle, badge, cover_style, link_type, link_target, scope, status, sort_no, effective_at, expire_at) VALUES
(2001,'NEWS','高才通2026下半年政策解读：A类门槛与续签要点','A类年薪门槛与合资格收入认定，续签在港实质要求','政策解读','NAVY','NONE',NULL,'NEWS_LIST','PUBLISHED',1,'2026-10-08 09:00:00',NULL),
(2002,'NEWS','DSE 报考人数创新高，内地考生如何布局','选科策略与港校内地双轨规划时间窗','国际教育','TEAL','NONE',NULL,'NEWS_LIST','PUBLISHED',2,'2026-10-06 09:00:00',NULL),
(2003,'NEWS','香港公司银行开户最新面签要求汇总','主流银行面签材料清单与预约节奏','跨境创业','GOLD','NONE',NULL,'NEWS_LIST','PUBLISHED',3,'2026-10-03 09:00:00',NULL);

INSERT INTO cms_content (id, content_type, title, subtitle, badge, cover_style, link_type, link_target, scope, status, sort_no, meta) VALUES
(3001,'CASE_STORY','深圳企业主，年薪达标却怕材料复杂。4周获批高才A类，全家受养同步，孩子衔接港校插班。','高才A类 + 2名受养人','4周获批','NAVY','SVC','1','HOME_STORY','PUBLISHED',1,'{"identity":"L先生 · 企业主","service":"高才A类 + 2名受养人"}'),
(3002,'CASE_STORY','全职妈妈重新出发读港硕，两个孩子免费入读官津学校，毕业留港身份与家庭两不误。','港硕进修 + 子女入学','2孩同读','PURPLE','SVC','3','HOME_STORY','PUBLISHED',2,'{"identity":"W女士 · 全职妈妈","service":"港硕进修 + 子女入学"}'),
(3003,'CASE_STORY','跨境电商卖家要稳妥续签：公司注册、银行开户、实体办公室一次办齐，注册地址挂靠让续签材料一次通过。','公司注册 + 办公室租约','续签一次过','GOLD','SVC','7','HOME_STORY','PUBLISHED',3,'{"identity":"Q先生 · 跨境电商","service":"公司注册 + 办公室租约"}'),
(3004,'CASE_STORY','内地转轨 DSE 不知从何下手：选科定位、中英文面试模拟逐项打磨，3 个月入读心仪中学。','DSE 规划 + 插班辅导','3个月录取','TEAL','SVC','4','HOME_STORY','PUBLISHED',4,'{"identity":"Z同学家长","service":"DSE 规划 + 插班辅导"}');
