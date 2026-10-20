-- 服务与交付（通用工单）、业财管理（定金押金、应收、收入结算）。

CREATE TABLE IF NOT EXISTS work_order (
    id           BIGINT        NOT NULL,
    title        VARCHAR(128)  NOT NULL COMMENT '工单标题',
    customer_id  BIGINT        DEFAULT NULL COMMENT '客户',
    assignee_id  BIGINT        DEFAULT NULL COMMENT '处理人 sys_user',
    status       VARCHAR(16)   NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN待处理 PROCESSING处理中 DONE已完成 CANCELED已取消',
    content      VARCHAR(1000) DEFAULT NULL COMMENT '处理要求',
    result       VARCHAR(1000) DEFAULT NULL COMMENT '处理结果',
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    BIGINT        DEFAULT NULL,
    update_by    BIGINT        DEFAULT NULL,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_work_order_status (status),
    KEY idx_work_order_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用工单';

CREATE TABLE IF NOT EXISTS work_order_log (
    id            BIGINT        NOT NULL,
    work_order_id BIGINT        NOT NULL,
    action        VARCHAR(16)   NOT NULL COMMENT 'CREATE START FINISH CANCEL NOTE',
    content       VARCHAR(1000) DEFAULT NULL,
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT        DEFAULT NULL,
    update_by     BIGINT        DEFAULT NULL,
    deleted       TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_work_order_log (work_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单处理记录';

CREATE TABLE IF NOT EXISTS deposit_record (
    id           BIGINT        NOT NULL,
    customer_id  BIGINT        NOT NULL,
    kind         VARCHAR(16)   NOT NULL COMMENT 'DEPOSIT定金 PLEDGE押金',
    amount       DECIMAL(12,2) NOT NULL,
    status       VARCHAR(16)   NOT NULL DEFAULT 'HELD' COMMENT 'HELD持有 REFUNDED已退 APPLIED已抵扣',
    remark       VARCHAR(255)  DEFAULT NULL,
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    BIGINT        DEFAULT NULL,
    update_by    BIGINT        DEFAULT NULL,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_deposit_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定金与押金';

CREATE TABLE IF NOT EXISTS receivable_bill (
    id           BIGINT        NOT NULL,
    customer_id  BIGINT        NOT NULL,
    title        VARCHAR(128)  NOT NULL,
    amount       DECIMAL(12,2) NOT NULL COMMENT '应收',
    received     DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '已收',
    status       VARCHAR(16)   NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN待收 PARTIAL部分 SETTLED已结清 VOID作废',
    due_date     DATE          DEFAULT NULL,
    remark       VARCHAR(255)  DEFAULT NULL,
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    BIGINT        DEFAULT NULL,
    update_by    BIGINT        DEFAULT NULL,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_receivable_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应收账单';

CREATE TABLE IF NOT EXISTS revenue_settlement (
    id           BIGINT        NOT NULL,
    customer_id  BIGINT        DEFAULT NULL,
    title        VARCHAR(128)  NOT NULL,
    amount       DECIMAL(12,2) NOT NULL,
    settle_date  DATE          NOT NULL,
    status       VARCHAR(16)   NOT NULL DEFAULT 'CONFIRMED' COMMENT 'CONFIRMED已确认 VOID作废',
    remark       VARCHAR(255)  DEFAULT NULL,
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    BIGINT        DEFAULT NULL,
    update_by    BIGINT        DEFAULT NULL,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收入结算';

UPDATE sys_menu SET sort_no = 6 WHERE id = 8 AND parent_id = 0;
UPDATE sys_menu SET sort_no = 7 WHERE id = 4 AND parent_id = 0;
UPDATE sys_menu SET sort_no = 8 WHERE id = 5 AND parent_id = 0;
UPDATE sys_menu SET sort_no = 9 WHERE id = 7 AND parent_id = 0;
UPDATE sys_menu SET sort_no = 10 WHERE id = 6 AND parent_id = 0;

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 9, 0, '服务与交付', NULL, 'Finished', NULL, 'DIR', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 9);
INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 91, 9, '工单管理', '/work-orders', 'Tickets', NULL, 'MENU', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 91);
INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 911, 91, '功能权限 · 工单处理', NULL, NULL, 'workorder:manage', 'BUTTON', 9 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 911);

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 10, 0, '业财管理', NULL, 'Wallet', NULL, 'DIR', 5 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 10);
INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 101, 10, '定金与押金', '/deposits', 'Wallet', NULL, 'MENU', 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 101);
INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 102, 10, '应收账单', '/receivables', 'Document', NULL, 'MENU', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 102);
INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 103, 10, '收入结算', '/settlements', 'Money', NULL, 'MENU', 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 103);
INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 1011, 101, '功能权限 · 业财维护', NULL, NULL, 'finance:manage', 'BUTTON', 9 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 1011);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 19009, 1, 9 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 19009);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 19091, 1, 91 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 19091);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 19911, 1, 911 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 19911);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 11010, 1, 10 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 11010);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 11101, 1, 101 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 11101);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 11102, 1, 102 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 11102);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 11103, 1, 103 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 11103);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 11111, 1, 1011 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 11111);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 29009, 2, 9 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 29009);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 29091, 2, 91 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 29091);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 29911, 2, 911 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 29911);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 21010, 2, 10 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 21010);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 21101, 2, 101 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 21101);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 21102, 2, 102 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 21102);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 21103, 2, 103 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 21103);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 21111, 2, 1011 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 21111);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 49009, 4, 9 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 49009);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 49091, 4, 91 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 49091);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 49911, 4, 911 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 49911);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 69009, 6, 9 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 69009);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 69091, 6, 91 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 69091);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 69911, 6, 911 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 69911);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 15010, 5, 10 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 15010);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 15101, 5, 101 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 15101);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 15102, 5, 102 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 15102);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 15103, 5, 103 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 15103);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 15111, 5, 1011 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 15111);
