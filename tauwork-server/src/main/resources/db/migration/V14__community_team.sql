-- 社区与团队：社区管理从「预约与社区」挪出，并增加顾问档案。
-- V1 的 consultant 是未使用的旧档案（0 行、无外键），这里换成社区顾问。

UPDATE sys_menu SET sort_no = 5 WHERE id = 4 AND parent_id = 0;
UPDATE sys_menu SET sort_no = 6 WHERE id = 5 AND parent_id = 0;
UPDATE sys_menu SET sort_no = 7 WHERE id = 7 AND parent_id = 0;
UPDATE sys_menu SET sort_no = 8 WHERE id = 6 AND parent_id = 0;

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 8, 0, '社区与团队', NULL, 'HomeFilled', NULL, 'DIR', 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 8);

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 81, 8, '顾问管理', '/consultants', 'Avatar', NULL, 'MENU', 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 81);

INSERT INTO sys_menu (id, parent_id, title, path, icon, perm, menu_type, sort_no)
SELECT 811, 81, '功能权限 · 顾问维护', NULL, NULL, 'team:manage', 'BUTTON', 9 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = 811);

UPDATE sys_menu SET parent_id = 8, title = '社区管理', sort_no = 1 WHERE id = 32;

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 81008, 1, 8 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 81008);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 81081, 1, 81 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 81081);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 81811, 1, 811 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 81811);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 82008, 2, 8 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 82008);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 82081, 2, 81 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 82081);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 82811, 2, 811 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 82811);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 84008, 4, 8 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 84008);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 84081, 4, 81 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 84081);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 84811, 4, 811 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 84811);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 86008, 6, 8 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 86008);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 86081, 6, 81 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 86081);
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 86811, 6, 811 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE id = 86811);

DROP TABLE IF EXISTS consultant;

CREATE TABLE consultant (
    id            BIGINT       NOT NULL,
    name          VARCHAR(32)  NOT NULL COMMENT '姓名',
    avatar_url    VARCHAR(255) DEFAULT NULL COMMENT '头像',
    title         VARCHAR(64)  DEFAULT NULL COMMENT '头衔',
    value_service VARCHAR(500) DEFAULT NULL COMMENT '价值服务',
    personality   VARCHAR(500) DEFAULT NULL COMMENT '性格特点',
    community_id  BIGINT       NOT NULL COMMENT '归属社区',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_consultant_community (community_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区顾问';
