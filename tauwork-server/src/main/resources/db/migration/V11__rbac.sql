-- 角色、菜单、功能权限、账号归属社区。预约登记与小程序咨询预约共用 svc_booking。

ALTER TABLE community
    ADD COLUMN booking_mode VARCHAR(32) DEFAULT NULL COMMENT '到店咨询方式，对应小程序 VIDEO 以外的 mode' AFTER status;

UPDATE community SET booking_mode = 'KWAI_TSING' WHERE id = 9001;
UPDATE community SET booking_mode = 'FUTIAN' WHERE id = 9002;
UPDATE community SET booking_mode = 'BAOAN' WHERE id = 9003;

CREATE TABLE sys_role (
    id          BIGINT       NOT NULL,
    code        VARCHAR(32)  NOT NULL COMMENT '角色编码',
    name        VARCHAR(64)  NOT NULL COMMENT '角色名称',
    remark      VARCHAR(255) DEFAULT NULL,
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    builtin     TINYINT      NOT NULL DEFAULT 0 COMMENT '1内置角色，编码不可改、不可删',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色';

CREATE TABLE sys_menu (
    id         BIGINT       NOT NULL,
    parent_id  BIGINT       NOT NULL DEFAULT 0,
    title      VARCHAR(64)  NOT NULL,
    path       VARCHAR(128) DEFAULT NULL COMMENT '前端路由，目录和按钮为空',
    icon       VARCHAR(64)  DEFAULT NULL,
    perm       VARCHAR(64)  DEFAULT NULL COMMENT '功能权限码',
    menu_type  VARCHAR(16)  NOT NULL COMMENT 'DIR/MENU/BUTTON',
    sort_no    INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单与功能权限';

CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色拥有的菜单和功能';

CREATE TABLE sys_user_community (
    id           BIGINT NOT NULL,
    user_id      BIGINT NOT NULL,
    community_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_community (user_id, community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账号归属社区，空表示不限（仅管理员）';

INSERT INTO sys_role (id, code, name, remark, status, builtin) VALUES
(1, 'ADMIN', '系统管理员', '全部菜单和功能', 1, 1),
(2, 'DIRECTOR', '运营总监', '经营、客户、社区、商品和内容', 1, 1),
(3, 'CONSULTANT', '顾问', '客户、线索、咨询预约和跟进', 1, 1),
(4, 'CASE_OPS', '案件运营', '社区预订与社区资料', 1, 1),
(5, 'FINANCE', '财务', '查看商品订单', 1, 1),
(6, 'SPACE_OPS', '空间运营', '社区、空间产品和商品', 1, 1);

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no) VALUES
(1,  0, '经营总览', NULL, 'DataLine', NULL, 'DIR', 1),
(11, 1, '经营驾驶舱', '/dashboard', 'Odometer', NULL, 'MENU', 1),
(2,  0, '客户与转化', NULL, 'User', NULL, 'DIR', 2),
(21, 2, '客户库', '/customers', 'Avatar', NULL, 'MENU', 1),
(22, 2, '线索池', '/leads', 'Flag', NULL, 'MENU', 2),
(23, 2, '预约登记', '/appointments', 'Calendar', NULL, 'MENU', 3),
(24, 2, '跟进日志', '/follows', 'EditPen', NULL, 'MENU', 4),
(211, 2, '功能权限 · 客户与跟进', NULL, NULL, 'crm:manage', 'BUTTON', 9),
(231, 23, '功能权限 · 预约调度', NULL, NULL, 'booking:manage', 'BUTTON', 9),
(3,  0, '预约与社区', NULL, 'Location', NULL, 'DIR', 3),
(31, 3, '社区预订订单', '/bookings', 'Tickets', NULL, 'MENU', 1),
(32, 3, '社区', '/communities', 'OfficeBuilding', NULL, 'MENU', 2),
(311, 3, '功能权限 · 社区与预订', NULL, NULL, 'space:manage', 'BUTTON', 9),
(4,  0, '产品与服务', NULL, 'Box', NULL, 'DIR', 4),
(41, 4, '通用商品', '/goods', 'Goods', NULL, 'MENU', 1),
(42, 4, '服务项目', '/services', 'Briefcase', NULL, 'MENU', 2),
(43, 4, '空间产品', '/space-products', 'House', NULL, 'MENU', 3),
(44, 4, '商品订单', '/goods-orders', 'ShoppingBag', NULL, 'MENU', 4),
(411, 4, '功能权限 · 商品与服务', NULL, NULL, 'product:manage', 'BUTTON', 9),
(5,  0, '内容运营', NULL, 'Notebook', NULL, 'DIR', 5),
(51, 5, '广告展示', '/cms/banner', 'Picture', NULL, 'MENU', 1),
(52, 5, '客户故事', '/cms/story', 'ChatLineSquare', NULL, 'MENU', 2),
(53, 5, '公告资讯', '/cms/news', 'Bell', NULL, 'MENU', 3),
(511, 5, '功能权限 · 内容管理', NULL, NULL, 'cms:manage', 'BUTTON', 9),
(6,  0, '系统管理', NULL, 'Setting', NULL, 'DIR', 6),
(61, 6, '用户管理', '/system/users', 'UserFilled', NULL, 'MENU', 1),
(62, 6, '角色权限', '/system/roles', 'Lock', NULL, 'MENU', 2),
(611, 61, '功能权限 · 用户维护', NULL, NULL, 'sys:user', 'BUTTON', 9),
(621, 62, '功能权限 · 角色维护', NULL, NULL, 'sys:role', 'BUTTON', 9);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 10000 + id, 1, id FROM sys_menu;

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 20000 + id, 2, id FROM sys_menu WHERE id NOT IN (6, 61, 62, 611, 621);

INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
(30011, 3, 1), (30012, 3, 11), (30021, 3, 2), (30022, 3, 21), (30023, 3, 22),
(30024, 3, 23), (30025, 3, 24), (30026, 3, 211), (30027, 3, 231);

INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
(40011, 4, 1), (40012, 4, 11), (40031, 4, 3), (40032, 4, 31), (40033, 4, 32), (40034, 4, 311);

INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
(50011, 5, 1), (50012, 5, 11), (50041, 5, 4), (50044, 5, 44);

INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
(60011, 6, 1), (60012, 6, 11),
(60031, 6, 3), (60032, 6, 31), (60033, 6, 32), (60034, 6, 311),
(60041, 6, 4), (60042, 6, 41), (60043, 6, 42), (60044, 6, 43), (60045, 6, 44), (60046, 6, 411);
