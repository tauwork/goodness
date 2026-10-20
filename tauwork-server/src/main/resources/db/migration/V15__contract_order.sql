-- 预约与社区改为合同与订单：合同、空间预订、商品订单。

UPDATE sys_menu SET title = '合同与订单', icon = 'Document' WHERE id = 3;
UPDATE sys_menu SET title = '预订订单', sort_no = 2 WHERE id = 31;
UPDATE sys_menu SET parent_id = 3, title = '商品订单', sort_no = 3 WHERE id = 44;

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 33, 3, '合同订单', '/contracts', 'Document', NULL, 'MENU', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 33);

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 331, 33, '功能权限 · 合同维护', NULL, NULL, 'contract:manage', 'BUTTON', 9 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 331);

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 441, 44, '功能权限 · 订单状态', NULL, NULL, 'order:manage', 'BUTTON', 9 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 441);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 13033, 1, 33 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 13033);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 13331, 1, 331 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 13331);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 13441, 1, 441 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 13441);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 23033, 2, 33 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 23033);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 23331, 2, 331 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 23331);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 23441, 2, 441 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 23441);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 53003, 5, 3 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 53003);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 53033, 5, 33 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 53033);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 53331, 5, 331 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 53331);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 53441, 5, 441 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 53441);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 63441, 6, 441 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 63441);

CREATE TABLE IF NOT EXISTS contract_order (
    id          BIGINT        NOT NULL,
    customer_id BIGINT        NOT NULL COMMENT '客户',
    amount      DECIMAL(12,2) NOT NULL COMMENT '合同金额',
    file_url    VARCHAR(512)  DEFAULT NULL COMMENT '合同附件',
    file_name   VARCHAR(255)  DEFAULT NULL COMMENT '附件原名',
    remark      VARCHAR(255)  DEFAULT NULL,
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   BIGINT        DEFAULT NULL,
    update_by   BIGINT        DEFAULT NULL,
    deleted     TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_contract_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同订单';
