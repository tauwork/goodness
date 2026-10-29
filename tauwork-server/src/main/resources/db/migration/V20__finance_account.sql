-- 业财配置：对公收款账户，小程序服务下单的对公转账读取这里。

CREATE TABLE IF NOT EXISTS finance_account (
    id            BIGINT       NOT NULL,
    account_name  VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '户名',
    bank_name     VARCHAR(128) NOT NULL DEFAULT '' COMMENT '开户行',
    account_no    VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '账号',
    branch        VARCHAR(128) NOT NULL DEFAULT '' COMMENT '支行',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收款账户';

INSERT INTO finance_account (id, account_name, bank_name, account_no, branch)
SELECT 1, '九千善', '', '', '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM finance_account WHERE id = 1);

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 104, 10, '业财配置', '/finance-config', 'Setting', NULL, 'MENU', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 104);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 11041, 1, 104 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 11041);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 21041, 2, 104 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 21041);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 51041, 5, 104 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 51041);
