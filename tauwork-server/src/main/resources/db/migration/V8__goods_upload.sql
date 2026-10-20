-- 通用商品：主图/详情/库存/规格；商品订单；分类「伴手礼」
-- 空间分类（工位/会议室/独立房间/公司地址/其他空间）保持不变

ALTER TABLE svc_product
    ADD COLUMN cover_url VARCHAR(512) DEFAULT NULL COMMENT '主图，相对地址 /files/...' AFTER subtitle,
    ADD COLUMN detail    VARCHAR(2000) DEFAULT NULL COMMENT '详情' AFTER cover_url,
    ADD COLUMN stock     INT           DEFAULT NULL COMMENT '通用商品库存件数，其他类型为空' AFTER price_value,
    ADD COLUMN spec      VARCHAR(64)   DEFAULT NULL COMMENT '通用商品规格' AFTER stock;

ALTER TABLE svc_product
    MODIFY COLUMN prod_type VARCHAR(16) NOT NULL DEFAULT 'SERVICE' COMMENT 'SERVICE服务/SPACE空间/GOODS通用商品';

INSERT INTO product_category (id, prod_type, code, name, icon, sort_no) VALUES
(301, 'GOODS', 'GIFT', '伴手礼', 'gift', 1);

CREATE TABLE goods_order (
    id           BIGINT        NOT NULL,
    order_id     BIGINT        NOT NULL COMMENT '关联 orders.id',
    order_no     VARCHAR(32)   NOT NULL,
    customer_id  BIGINT        NOT NULL,
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(128)  NOT NULL,
    spec         VARCHAR(64)   DEFAULT NULL,
    cover_url    VARCHAR(512)  DEFAULT NULL,
    qty          INT           NOT NULL,
    unit_price   DECIMAL(12,2) NOT NULL,
    amount       DECIMAL(12,2) NOT NULL,
    remark       VARCHAR(255)  DEFAULT NULL,
    status       VARCHAR(24)   NOT NULL DEFAULT 'PENDING_PAY' COMMENT 'PENDING_PAY待支付/PAID已支付/DONE已交付/CANCELED已取消',
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    BIGINT        DEFAULT NULL,
    update_by    BIGINT        DEFAULT NULL,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order (order_id),
    KEY idx_customer (customer_id, create_time),
    KEY idx_product (product_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用商品订单';
