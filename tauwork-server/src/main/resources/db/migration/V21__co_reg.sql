-- 公司注册工单：下单后按香港公司注册流程收集资料并核对。

CREATE TABLE IF NOT EXISTS co_reg_case (
    id               BIGINT        NOT NULL,
    order_no         VARCHAR(32)   NOT NULL,
    svc_order_id     BIGINT        NOT NULL,
    customer_id      BIGINT        NOT NULL,
    product_id       BIGINT        NOT NULL,
    product_name     VARCHAR(128)  NOT NULL,
    step             VARCHAR(16)   NOT NULL DEFAULT 'NAME' COMMENT 'NAME核名 PROFILE资料 KYC身份 SIGN签署 FILING递交 CERT领证 DONE完成',
    step_status      VARCHAR(16)   NOT NULL DEFAULT 'WAIT_USER' COMMENT 'WAIT_USER待客户 WAIT_REVIEW待核对 REJECTED退回 WAIT_OPS待运营 DONE完成',
    case_status      VARCHAR(16)   NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE进行中 DONE已完成 CANCELED已取消',
    company_name     VARCHAR(128)  DEFAULT NULL COMMENT '拟用中文名称',
    company_name_en  VARCHAR(128)  DEFAULT NULL COMMENT '拟用英文名称',
    biz_scope        VARCHAR(500)  DEFAULT NULL COMMENT '主营业务',
    capital_amount   DECIMAL(12,2) NOT NULL DEFAULT 10000.00 COMMENT '注册资金',
    capital_currency VARCHAR(8)    NOT NULL DEFAULT 'HKD',
    remark           VARCHAR(255)  DEFAULT NULL COMMENT '最近一次退回或备妥说明',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by        BIGINT        DEFAULT NULL,
    update_by        BIGINT        DEFAULT NULL,
    deleted          TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_co_reg_order (order_no),
    KEY idx_co_reg_customer (customer_id, create_time),
    KEY idx_co_reg_step (step, step_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司注册工单';

CREATE TABLE IF NOT EXISTS co_reg_member (
    id           BIGINT        NOT NULL,
    case_id      BIGINT        NOT NULL,
    name         VARCHAR(64)   NOT NULL,
    role         VARCHAR(16)   NOT NULL COMMENT 'SHAREHOLDER股东 DIRECTOR董事 BOTH股东兼董事',
    share_ratio  DECIMAL(6,2)  NOT NULL DEFAULT 0 COMMENT '持股比例，董事可为0',
    id_front_url VARCHAR(512)  DEFAULT NULL,
    id_back_url  VARCHAR(512)  DEFAULT NULL,
    id_hold_url  VARCHAR(512)  DEFAULT NULL,
    sort_no      INT           NOT NULL DEFAULT 0,
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    BIGINT        DEFAULT NULL,
    update_by    BIGINT        DEFAULT NULL,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_co_reg_member_case (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司注册股东及董事';

CREATE TABLE IF NOT EXISTS co_reg_log (
    id          BIGINT       NOT NULL,
    case_id     BIGINT       NOT NULL,
    step        VARCHAR(16)  NOT NULL,
    action      VARCHAR(32)  NOT NULL,
    remark      VARCHAR(255) DEFAULT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_co_reg_log_case (case_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司注册工单进度';

INSERT INTO co_reg_case (id, order_no, svc_order_id, customer_id, product_id, product_name, step, step_status, case_status, capital_amount, capital_currency, create_time, update_time, deleted)
SELECT o.id, o.order_no, o.id, o.customer_id, o.product_id, o.product_name, 'NAME', 'WAIT_USER',
       IF(o.status = 'CANCELED', 'CANCELED', 'ACTIVE'), 10000, 'HKD', o.create_time, NOW(), 0
FROM svc_order o
JOIN svc_product p ON p.id = o.product_id AND p.deleted = 0
WHERE o.deleted = 0
  AND p.name LIKE '%公司注册%'
  AND NOT EXISTS (SELECT 1 FROM co_reg_case x WHERE x.order_no = o.order_no);

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 92, 9, '公司注册工单', '/co-reg', 'OfficeBuilding', NULL, 'MENU', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 92);
INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 921, 92, '功能权限 · 注册核对', NULL, NULL, 'workorder:manage', 'BUTTON', 9 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 921);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 19092, 1, 92 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 19092);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 19921, 1, 921 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 19921);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 29092, 2, 92 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 29092);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 29921, 2, 921 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 29921);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 49092, 4, 92 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 49092);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 49921, 4, 921 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 49921);
