-- ============================================================
-- V0.3 产品模型升级 + 社区管理
-- 设计参考 WeWork / DoBe Hub：社区店(Location) → 空间产品(Space Product) 两级结构
-- 1. 产品分类可配置（product_category，服务类/空间类共用一张分类表）
-- 2. 产品特征参数可配置（product_attr_def，分类级参数模板，产品存 attrs JSON）
-- 3. 社区店（community）：一个具体空间/网点
-- 4. 社区×产品关联（community_product）：同一空间产品可在不同社区店售卖，价格/库存社区级覆盖
-- ============================================================

-- 服务产品表升级：挂分类 + 扩展为双类型 + 特征参数值
ALTER TABLE svc_product
    ADD COLUMN prod_type   VARCHAR(16) NOT NULL DEFAULT 'SERVICE' COMMENT 'SERVICE服务/SPACE空间' AFTER id,
    ADD COLUMN category_id BIGINT      DEFAULT NULL COMMENT '分类 product_category.id' AFTER prod_type,
    ADD COLUMN attrs       JSON        DEFAULT NULL COMMENT '特征参数值 {attr_code: value}';

-- 产品分类（可配置）：服务类=公司注册/香港身份/国际教育/续签…；空间类=工位/独立房间/会议室/公司地址/其他
CREATE TABLE product_category (
    id         BIGINT      NOT NULL,
    prod_type  VARCHAR(16) NOT NULL COMMENT 'SERVICE/SPACE',
    code       VARCHAR(32) NOT NULL COMMENT '分类编码',
    name       VARCHAR(64) NOT NULL COMMENT '分类名称',
    icon       VARCHAR(64) DEFAULT NULL COMMENT '图标名（小程序端映射）',
    parent_id  BIGINT      DEFAULT NULL COMMENT '父分类（预留二级）',
    sort_no    INT         NOT NULL DEFAULT 0,
    status     TINYINT     NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    create_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by  BIGINT      DEFAULT NULL,
    update_by  BIGINT      DEFAULT NULL,
    deleted    TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_type_code (prod_type, code, deleted),
    KEY idx_type (prod_type, status, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品分类（可配置）';

-- 产品特征参数定义（分类级模板，产品填写 attrs JSON）
CREATE TABLE product_attr_def (
    id          BIGINT       NOT NULL,
    category_id BIGINT       NOT NULL COMMENT '所属分类',
    attr_code   VARCHAR(32)  NOT NULL COMMENT '参数编码，如 capacity/has_projector',
    attr_name   VARCHAR(64)  NOT NULL COMMENT '参数名，如 容纳人数',
    input_type  VARCHAR(16)  NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT/NUMBER/SELECT/BOOLEAN',
    options     JSON         DEFAULT NULL COMMENT 'SELECT 选项 [{value,label}]',
    unit        VARCHAR(16)  DEFAULT NULL COMMENT '单位，如 人/小时/月',
    required    TINYINT      NOT NULL DEFAULT 0,
    sort_no     INT          NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品特征参数定义';

-- 社区店（一个具体空间，参考 WeWork Location / DoBe Hub 社区）
CREATE TABLE community (
    id          BIGINT       NOT NULL,
    name        VARCHAR(64)  NOT NULL COMMENT '社区店名，如 香港·葵青总部空间',
    city        VARCHAR(32)  NOT NULL COMMENT '城市：深圳/香港/新加坡…',
    district    VARCHAR(64)  DEFAULT NULL COMMENT '区域',
    address     VARCHAR(255) NOT NULL COMMENT '详细地址',
    lat         DECIMAL(10,6) DEFAULT NULL COMMENT '纬度（地图导航）',
    lng         DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
    open_hours  VARCHAR(64)  DEFAULT NULL COMMENT '开放时间',
    cover_url   VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    facilities  JSON         DEFAULT NULL COMMENT '设施标签 ["投屏","茶水","Wi-Fi"]',
    intro       VARCHAR(512) DEFAULT NULL COMMENT '简介',
    status      VARCHAR(16)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN营业/PREPARING筹备中/CLOSED',
    sort_no     INT          NOT NULL DEFAULT 0,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_city (city, status, sort_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区店';

-- 社区×产品关联：同一产品在不同社区店售卖，社区级价格/库存/单位覆盖
CREATE TABLE community_product (
    id            BIGINT        NOT NULL,
    community_id  BIGINT        NOT NULL,
    product_id    BIGINT        NOT NULL,
    price         DECIMAL(12,2) DEFAULT NULL COMMENT '社区级价格（空=用产品默认价）',
    price_unit    VARCHAR(16)   DEFAULT NULL COMMENT 'HOUR/DAY/MONTH/ONCE/NULL按报价',
    stock         INT           DEFAULT NULL COMMENT '库存（工位席数/房间间数；NULL=不限）',
    on_sale       TINYINT       NOT NULL DEFAULT 1,
    sort_no       INT           NOT NULL DEFAULT 0,
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT        DEFAULT NULL,
    update_by     BIGINT        DEFAULT NULL,
    deleted       TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_c_p (community_id, product_id, deleted),
    KEY idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区产品关联';

-- ==================== 种子数据 ====================

-- 服务分类
INSERT INTO product_category (id, prod_type, code, name, icon, sort_no) VALUES
(101,'SERVICE','HK_IDENTITY','香港身份','passport',1),
(102,'SERVICE','COMPANY','公司注册与合规','building',2),
(103,'SERVICE','EDU','国际教育','cap',3),
(104,'SERVICE','RENEW','续签与永居','shield',4),
(105,'SERVICE','OTHER_SVC','其他服务','grid',9);

-- 空间分类（用户指定五类：工位/房间/会议室/公司地址/其他）
INSERT INTO product_category (id, prod_type, code, name, icon, sort_no) VALUES
(201,'SPACE','DESK','开放工位','seat',1),
(202,'SPACE','ROOM','独立房间','door',2),
(203,'SPACE','MEETING','会议室','meeting',3),
(204,'SPACE','ADDR','公司地址','pin',4),
(205,'SPACE','OTHER_SPACE','其他空间','grid',9);

-- 空间分类特征参数定义
INSERT INTO product_attr_def (id, category_id, attr_code, attr_name, input_type, options, unit, required, sort_no) VALUES
(3001,201,'seats','工位席数','NUMBER',NULL,'席',1,1),
(3002,202,'capacity','容纳人数','NUMBER',NULL,'人',1,1),
(3003,202,'furnished','是否带家具','BOOLEAN',NULL,NULL,0,2),
(3004,203,'capacity','容纳人数','NUMBER',NULL,'人',1,1),
(3005,203,'facility','会议设施','SELECT','[{"value":"screen","label":"投屏"},{"value":"vc","label":"视频会议系统"},{"value":"wb","label":"白板"},{"value":"mic","label":"麦克风音响"}]',NULL,0,2),
(3006,203,'quiet','安静等级','SELECT','[{"value":"s","label":"安静区"},{"value":"n","label":"普通"}]',NULL,0,3),
(3007,204,'addr_type','地址类型','SELECT','[{"value":"reg","label":"注册挂靠"},{"value":"vir","label":"虚拟办公"},{"value":"phys","label":"实体办公"}]',NULL,1,1),
(3008,204,'mail_service','信件代收','BOOLEAN',NULL,NULL,0,2),
(3009,205,'note','说明','TEXT',NULL,NULL,0,1);

-- 服务产品挂分类（沿用 V1 已有产品，此处更新 + 补空间产品）
UPDATE svc_product SET prod_type='SERVICE', category_id=101 WHERE id=1;  -- 高才通
UPDATE svc_product SET prod_type='SERVICE', category_id=101 WHERE id=2;  -- 优才
UPDATE svc_product SET prod_type='SERVICE', category_id=101 WHERE id=3;  -- 港硕进修
UPDATE svc_product SET prod_type='SERVICE', category_id=103 WHERE id=4;  -- DSE
UPDATE svc_product SET prod_type='SERVICE', category_id=103 WHERE id=5;  -- 家长课堂
UPDATE svc_product SET prod_type='SERVICE', category_id=102 WHERE id=6;  -- 公司注册+开户
UPDATE svc_product SET prod_type='SERVICE', category_id=102 WHERE id=7;  -- 年审审计

-- 空间产品（与小程序原型 v1.1 空间 Tab 一致）
INSERT INTO svc_product (id, prod_type, category_id, name, subtitle, price_mode, price_value, attrs, on_sale, sort_no) VALUES
(5001,'SPACE',201,'开放工位 · 日卡','自由工位随到随坐 · 含咖啡茶水','FIXED',88.00,'{"seats":60}',1,1),
(5002,'SPACE',203,'莲花厅 · 会议室','投屏+视频会议系统+白板，路演级','FIXED',180.00,'{"capacity":12,"facility":["screen","vc","wb","mic"]}',1,2),
(5003,'SPACE',203,'青云室 · 会议室','投屏+白板，顾问一对一咨询','FIXED',120.00,'{"capacity":6,"facility":["screen","wb"],"quiet":"s"}',1,3),
(5004,'SPACE',202,'独立办公室 · 2人间','锁门独立空间','FIXED',3800.00,'{"capacity":2,"furnished":true}',1,4),
(5005,'SPACE',202,'独立办公室 · 4-6人间','初创团队','FIXED',6800.00,'{"capacity":6,"furnished":true}',1,5),
(5006,'SPACE',204,'公司注册地址 · 挂靠','注册地址+水牌+信件代收，服务高才/专才续签','FIXED',300.00,'{"addr_type":"reg","mail_service":true}',1,6),
(5007,'SPACE',203,'路演厅 · 活动场地','阶梯式 · 投屏音响齐备','FIXED',500.00,'{"capacity":50,"facility":["screen","mic"]}',1,7);

-- 社区店（三大网点，对应原型）
INSERT INTO community (id, name, city, district, address, open_hours, facilities, intro, status, sort_no) VALUES
(9001,'香港 · 葵青总部空间','香港','葵青区','长荣工业大厦2楼201-205室','周一至周日 09:00-21:00','["投屏","视频会议","茶水咖啡","前台接待","Wi-Fi"]','全球总部 · 会议室4间/工位60+/独立办公室12间','OPEN',1),
(9002,'深圳 · 福田空间','深圳','福田区','中国凤凰大厦2栋509室','周一至周五 09:00-21:00','["投屏","Wi-Fi","茶水"]','会展中心商圈 · 咨询面谈主场地','OPEN',2),
(9003,'深圳 · 宝安空间','深圳','宝安区','固戍一路888号股份公司综合楼403室','周一至周六 09:00-20:00','["投屏","Wi-Fi"]','周末家长课堂主场地','OPEN',3);

-- 社区×产品关联（葵青全量；福田/宝安部分）
INSERT INTO community_product (id, community_id, product_id, price, price_unit, stock, sort_no) VALUES
(70001,9001,5001,88.00,'DAY',60,1),
(70002,9001,5002,180.00,'HOUR',NULL,2),
(70003,9001,5003,120.00,'HOUR',NULL,3),
(70004,9001,5004,3800.00,'MONTH',2,4),
(70005,9001,5005,6800.00,'MONTH',4,5),
(70006,9001,5006,300.00,'MONTH',NULL,6),
(70007,9001,5007,500.00,'ONCE',NULL,7),
(70008,9002,5003,100.00,'HOUR',NULL,1),
(70009,9002,5001,68.00,'DAY',24,2),
(70010,9003,5003,80.00,'HOUR',NULL,1),
(70011,9003,5001,58.00,'DAY',16,2);
