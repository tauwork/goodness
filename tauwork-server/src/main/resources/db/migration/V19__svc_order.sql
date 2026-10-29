-- 服务下单：在线支付或对公转账，凭证由后台确认到账。

CREATE TABLE IF NOT EXISTS svc_order (
    id           BIGINT        NOT NULL,
    order_id     BIGINT        NOT NULL COMMENT '关联 orders.id',
    order_no     VARCHAR(32)   NOT NULL,
    customer_id  BIGINT        NOT NULL,
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(128)  NOT NULL,
    cover_url    VARCHAR(512)  DEFAULT NULL,
    amount       DECIMAL(12,2) NOT NULL,
    pay_channel  VARCHAR(16)   NOT NULL COMMENT 'WECHAT在线支付 TRANSFER对公转账',
    status       VARCHAR(24)   NOT NULL DEFAULT 'PENDING_PAY' COMMENT 'PENDING_PAY待支付 CONFIRMING待确认到账 PAID已支付 CANCELED已取消',
    voucher_url  VARCHAR(512)  DEFAULT NULL COMMENT '转账凭证',
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    BIGINT        DEFAULT NULL,
    update_by    BIGINT        DEFAULT NULL,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_svc_order (order_id),
    KEY idx_svc_order_customer (customer_id, create_time),
    KEY idx_svc_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务订单';

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 47, 3, '服务订单', '/svc-orders', 'Tickets', NULL, 'MENU', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 47);

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 471, 47, '功能权限 · 确认到账', NULL, NULL, 'order:manage', 'BUTTON', 9 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 471);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 14701, 1, 47 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 14701);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 14711, 1, 471 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 14711);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 24701, 2, 47 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 24701);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 24711, 2, 471 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 24711);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 54701, 5, 47 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 54701);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 54711, 5, 471 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 54711);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 64701, 6, 47 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 64701);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 64711, 6, 471 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 64711);
