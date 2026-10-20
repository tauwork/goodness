-- 合伙人：分享链接或后台指定归属；按产品类目计提成。

ALTER TABLE customer
    ADD COLUMN partner_id BIGINT DEFAULT NULL COMMENT '归属合伙人 partner.id' AFTER source,
    ADD COLUMN partner_bind_at DATETIME DEFAULT NULL COMMENT '归属时间' AFTER partner_id,
    ADD COLUMN partner_bind_source VARCHAR(16) DEFAULT NULL COMMENT 'LINK分享 / ADMIN后台指定' AFTER partner_bind_at;

CREATE TABLE partner (
    id           BIGINT      NOT NULL,
    customer_id  BIGINT      NOT NULL COMMENT '合伙人自己的客户档案',
    code         VARCHAR(16) NOT NULL COMMENT '分享码，链接参数 partner',
    status       TINYINT     NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    BIGINT      DEFAULT NULL,
    update_by    BIGINT      DEFAULT NULL,
    deleted      TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_partner_customer (customer_id),
    UNIQUE KEY uk_partner_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合伙人';

CREATE TABLE partner_rule (
    id           BIGINT        NOT NULL,
    category_id  BIGINT        NOT NULL COMMENT '产品类目',
    rate         DECIMAL(5,2)  NOT NULL DEFAULT 0 COMMENT '提成比例 %',
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    BIGINT        DEFAULT NULL,
    update_by    BIGINT        DEFAULT NULL,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_partner_rule_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='类目提成比例';

CREATE TABLE partner_commission (
    id                BIGINT        NOT NULL,
    partner_id        BIGINT        NOT NULL,
    customer_id       BIGINT        NOT NULL COMMENT '成交客户',
    order_id          BIGINT        DEFAULT NULL,
    order_no          VARCHAR(32)   DEFAULT NULL,
    product_id        BIGINT        DEFAULT NULL,
    product_name      VARCHAR(128)  DEFAULT NULL,
    category_id       BIGINT        DEFAULT NULL,
    category_name     VARCHAR(64)   DEFAULT NULL,
    order_amount      DECIMAL(12,2) NOT NULL,
    rate              DECIMAL(5,2)  NOT NULL,
    commission_amount DECIMAL(12,2) NOT NULL,
    status            VARCHAR(16)   NOT NULL COMMENT 'ACCRUED计入 / REVERSED冲回',
    source            VARCHAR(16)   NOT NULL COMMENT 'ORDER支付 / MANUAL登记',
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by         BIGINT        DEFAULT NULL,
    update_by         BIGINT        DEFAULT NULL,
    deleted           TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_commission_partner (partner_id, status),
    KEY idx_commission_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合伙人提成明细';

INSERT INTO partner_rule (id, category_id, rate)
SELECT 800000 + id, id, 8.00 FROM product_category WHERE deleted = 0;

UPDATE sys_menu SET sort_no = 7 WHERE id = 6;

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no) VALUES
(7,   0, '合伙人管理', NULL, 'Share', NULL, 'DIR', 6),
(71,  7, '合伙人列表', '/partners', 'User', NULL, 'MENU', 1),
(72,  7, '提成规则', '/partner-rules', 'Coin', NULL, 'MENU', 2),
(711, 71, '功能权限 · 合伙人维护', NULL, NULL, 'partner:manage', 'BUTTON', 9);

INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
(71007, 1, 7), (71071, 1, 71), (71072, 1, 72), (71711, 1, 711),
(72007, 2, 7), (72071, 2, 71), (72072, 2, 72), (72711, 2, 711);
