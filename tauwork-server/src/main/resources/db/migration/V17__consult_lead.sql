-- 1v1 咨询：顾问联系方式、服务对接顾问、线索需求描述

ALTER TABLE consultant
    ADD COLUMN wechat VARCHAR(64) NULL COMMENT '微信号' AFTER personality,
    ADD COLUMN phone VARCHAR(20) NULL COMMENT '联系电话' AFTER wechat;

ALTER TABLE svc_product
    ADD COLUMN consultant_id BIGINT NULL COMMENT '对接顾问 consultant.id' AFTER category_id;

ALTER TABLE `lead`
    ADD COLUMN remark VARCHAR(1000) NULL COMMENT '需求描述' AFTER intent;
