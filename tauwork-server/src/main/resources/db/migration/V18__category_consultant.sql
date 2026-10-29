-- 服务大类对接顾问，1v1 咨询按分类展示联系方式

ALTER TABLE product_category
    ADD COLUMN consultant_id BIGINT NULL COMMENT '对接顾问 consultant.id' AFTER sort_no;
