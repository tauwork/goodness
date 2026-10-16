-- V0.4.1 内容管理增强：置顶标记
ALTER TABLE cms_content
    ADD COLUMN is_top TINYINT NOT NULL DEFAULT 0 COMMENT '置顶 1/0，置顶内容排最前' AFTER status;
