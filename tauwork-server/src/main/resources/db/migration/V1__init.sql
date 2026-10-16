-- ============================================================
-- Tauwork 九千善数智平台 V1__init.sql
-- 12 张核心表（MVP）：账号/顾问/客户家庭/线索/预约/产品/评估/订单/案件流程/材料/消息
-- 字符集 utf8mb4，InnoDB；所有表带审计字段与逻辑删除
-- ============================================================

-- 1. 后台账号
CREATE TABLE sys_user (
    id            BIGINT       NOT NULL COMMENT '主键',
    username      VARCHAR(64)  NOT NULL COMMENT '登录名',
    password_hash VARCHAR(128) NOT NULL COMMENT '密码哈希(bcrypt)',
    real_name     VARCHAR(64)  NOT NULL COMMENT '姓名',
    role_code     VARCHAR(32)  NOT NULL DEFAULT 'CONSULTANT' COMMENT '角色: ADMIN/DIRECTOR/CONSULTANT/CASE_OPS/FINANCE/SPACE_OPS',
    phone         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    last_login_at DATETIME     DEFAULT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台账号';

-- 2. 顾问（业务档案，关联 sys_user）
CREATE TABLE consultant (
    id            BIGINT       NOT NULL,
    user_id       BIGINT       NOT NULL COMMENT '关联sys_user.id',
    title         VARCHAR(64)  DEFAULT NULL COMMENT '头衔，如 身份规划总监',
    dept_code     VARCHAR(32)  NOT NULL DEFAULT 'IDENTITY' COMMENT '部门: IDENTITY/EDU/STARTUP/CASE_OPS/SPACE',
    expertise     VARCHAR(255) DEFAULT NULL COMMENT '擅长领域，逗号分隔',
    case_count    INT          NOT NULL DEFAULT 0 COMMENT '累计案例数',
    score         DECIMAL(2,1) NOT NULL DEFAULT 5.0 COMMENT '客户评分',
    on_duty       TINYINT      NOT NULL DEFAULT 1 COMMENT '是否接单',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾问';

-- 3. 客户（家庭主账号）
CREATE TABLE customer (
    id            BIGINT       NOT NULL,
    wx_openid     VARCHAR(64)  DEFAULT NULL COMMENT '微信openid（小程序客户）',
    name          VARCHAR(64)  DEFAULT NULL COMMENT '姓名（脱敏展示）',
    phone         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    phone_hash    VARCHAR(64)  DEFAULT NULL COMMENT '手机号哈希（防重复检索）',
    owner_id      BIGINT       DEFAULT NULL COMMENT '归属顾问 consultant.id',
    stage         VARCHAR(32)  NOT NULL DEFAULT 'NEW' COMMENT 'NEW/ASSESSING/APPOINTED/SIGNED/RENEWING/LOST',
    source        VARCHAR(32)  DEFAULT NULL COMMENT 'ASSESS/CONTENT/REFERRAL/EVENT/RENEWAL_CAL/OTHER',
    family_note   VARCHAR(512) DEFAULT NULL COMMENT '家庭情况备注',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_openid (wx_openid, deleted),
    KEY idx_phone (phone_hash),
    KEY idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户（家庭主账号）';

-- 4. 家庭成员（多孩管理）
CREATE TABLE family_member (
    id            BIGINT      NOT NULL,
    customer_id   BIGINT      NOT NULL COMMENT '所属customer.id',
    relation      VARCHAR(16) NOT NULL COMMENT 'SELF/SPOUSE/CHILD',
    name          VARCHAR(64) DEFAULT NULL,
    birth_date    DATE        DEFAULT NULL,
    school_stage  VARCHAR(32) DEFAULT NULL COMMENT '在读阶段',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT      DEFAULT NULL,
    update_by     BIGINT      DEFAULT NULL,
    deleted       TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭成员';

-- 5. 线索（CRM 线索池）
CREATE TABLE `lead` (
    id            BIGINT       NOT NULL,
    customer_id   BIGINT       DEFAULT NULL COMMENT '已转化客户id',
    name          VARCHAR(64)  DEFAULT NULL,
    phone         VARCHAR(20)  DEFAULT NULL,
    source        VARCHAR(32)  NOT NULL COMMENT 'ASSESS/BOOKING/CONTENT/REFERRAL/EVENT/RENEWAL_CAL',
    intent        VARCHAR(64)  DEFAULT NULL COMMENT '业务意向',
    assess_score  INT          DEFAULT NULL COMMENT '评估分',
    status        VARCHAR(32)  NOT NULL DEFAULT 'NEW' COMMENT 'NEW/ASSIGNED/IN_PROGRESS/CONVERTED/PUBLIC_POOL',
    owner_id      BIGINT       DEFAULT NULL COMMENT '分配顾问',
    assigned_at   DATETIME     DEFAULT NULL,
    last_follow_at DATETIME    DEFAULT NULL COMMENT '最近跟进时间（超72h自动回收）',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索池';

-- 6. 咨询预约
CREATE TABLE booking (
    id            BIGINT       NOT NULL,
    customer_id   BIGINT       NOT NULL,
    consultant_id BIGINT       DEFAULT NULL COMMENT '顾问，空=待分配',
    topic         VARCHAR(64)  NOT NULL COMMENT '咨询主题',
    mode          VARCHAR(32)  NOT NULL COMMENT 'VIDEO/FUTIAN/BAOAN/KWAI_TSING',
    book_date     DATE         NOT NULL,
    time_slot     VARCHAR(16)  NOT NULL COMMENT 'HH:mm',
    status        VARCHAR(32)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/DONE/CANCELED/NO_SHOW',
    remark        VARCHAR(512) DEFAULT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_slot (consultant_id, book_date, time_slot, deleted),
    KEY idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询预约';

-- 7. 服务产品目录
CREATE TABLE svc_product (
    id            BIGINT       NOT NULL,
    biz_line      VARCHAR(32)  DEFAULT NULL COMMENT 'IDENTITY/EDU/STARTUP（旧数据兼容；新数据用 category_id）',
    name          VARCHAR(128) NOT NULL,
    subtitle      VARCHAR(255) DEFAULT NULL,
    price_mode    VARCHAR(32)  NOT NULL DEFAULT 'QUOTE' COMMENT 'QUOTE评估后报价/FIXED固定/STAGED按阶段/MEMBER会员专享',
    price_value   DECIMAL(12,2) DEFAULT NULL COMMENT 'FIXED时的价格',
    commission_l1 DECIMAL(5,2) NOT NULL DEFAULT 8.00 COMMENT '一级佣金%',
    commission_l2 DECIMAL(5,2) NOT NULL DEFAULT 3.00 COMMENT '二级佣金%',
    flow_tpl_id   BIGINT       DEFAULT NULL COMMENT '关联流程模板',
    material_tpl  JSON         DEFAULT NULL COMMENT '材料清单模板',
    on_sale       TINYINT      NOT NULL DEFAULT 1,
    sort_no       INT          NOT NULL DEFAULT 0,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_line (biz_line, on_sale)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务产品';

-- 8. 智能评估提交
CREATE TABLE assess_submission (
    id            BIGINT       NOT NULL,
    wx_openid     VARCHAR(64)  DEFAULT NULL,
    lead_id       BIGINT       DEFAULT NULL COMMENT '生成的线索',
    answers       JSON         NOT NULL COMMENT '答题结果 {code: value}',
    score         INT          NOT NULL COMMENT '综合适配度',
    recommend     JSON         NOT NULL COMMENT '推荐路径 [{code,fit}]',
    version       VARCHAR(16)  NOT NULL DEFAULT 'v1' COMMENT '题库版本',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_openid (wx_openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估提交';

-- 9. 订单
CREATE TABLE orders (
    id            BIGINT       NOT NULL,
    order_no      VARCHAR(32)  NOT NULL COMMENT 'PO-/SP-/LS-',
    type          VARCHAR(16)  NOT NULL COMMENT 'SERVICE服务/SPACE空间/LEASE续费',
    customer_id   BIGINT       NOT NULL,
    product_id    BIGINT       DEFAULT NULL,
    product_name  VARCHAR(128) NOT NULL,
    amount        DECIMAL(12,2) NOT NULL,
    pay_status    VARCHAR(32)  NOT NULL DEFAULT 'UNPAID' COMMENT 'UNPAID/PAID/INSTALLMENT/REFUNDED',
    pay_channel   VARCHAR(32)  DEFAULT NULL COMMENT 'OFFLINE/WECHAT/AUTO_DEBIT',
    case_id       BIGINT       DEFAULT NULL COMMENT '关联案件（服务单）',
    dist_ref_id   BIGINT       DEFAULT NULL COMMENT '分销推荐人（v1.1）',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- 10. 案件
CREATE TABLE case_main (
    id            BIGINT       NOT NULL,
    case_no       VARCHAR(32)  NOT NULL COMMENT 'TAU-2026-xxxx',
    customer_id   BIGINT       NOT NULL,
    product_id    BIGINT       NOT NULL,
    order_id      BIGINT       DEFAULT NULL,
    owner_id      BIGINT       NOT NULL COMMENT '负责顾问',
    flow_tpl_id   BIGINT       NOT NULL,
    current_node  INT          NOT NULL DEFAULT 0 COMMENT '当前节点序号',
    status        VARCHAR(32)  NOT NULL DEFAULT 'PROCESSING' COMMENT 'PROCESSING/DONE/SUSPENDED',
    due_date      DATE         DEFAULT NULL COMMENT '预计完成',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_case_no (case_no),
    KEY idx_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件';

-- 11. 案件流程模板 + 节点
CREATE TABLE flow_template (
    id            BIGINT       NOT NULL,
    biz_type      VARCHAR(32)  NOT NULL COMMENT 'GAOCAI/YOUCAI/HKPG/COMPANY/ANNUAL/EDU',
    name          VARCHAR(64)  NOT NULL,
    node_count    INT          NOT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程模板';

CREATE TABLE flow_template_node (
    id            BIGINT       NOT NULL,
    tpl_id        BIGINT       NOT NULL,
    seq           INT          NOT NULL,
    name          VARCHAR(64)  NOT NULL,
    role_code     VARCHAR(32)  NOT NULL COMMENT '责任角色',
    sla_days      INT          DEFAULT NULL COMMENT '标准工期',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_tpl (tpl_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程模板节点';

-- 12. 案件节点实例（进度时间线）
CREATE TABLE case_node (
    id            BIGINT       NOT NULL,
    case_id       BIGINT       NOT NULL,
    seq           INT          NOT NULL,
    name          VARCHAR(64)  NOT NULL,
    status        VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CURRENT/DONE',
    done_at       DATETIME     DEFAULT NULL,
    note          VARCHAR(255) DEFAULT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_case (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='案件节点';

-- 13. 材料项
CREATE TABLE material_item (
    id            BIGINT       NOT NULL,
    case_id       BIGINT       NOT NULL,
    group_name    VARCHAR(64)  NOT NULL COMMENT '分组，如 资产/纳税证明',
    name          VARCHAR(128) NOT NULL,
    required      TINYINT      NOT NULL DEFAULT 1,
    status        VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/UPLOADED/APPROVED/REJECTED',
    file_key      VARCHAR(255) DEFAULT NULL COMMENT '对象存储key（加密桶）',
    reject_reason VARCHAR(255) DEFAULT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_case (case_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='材料项';

-- 14. 消息通知记录
CREATE TABLE message_log (
    id            BIGINT       NOT NULL,
    wx_openid     VARCHAR(64)  NOT NULL,
    scene         VARCHAR(32)  NOT NULL COMMENT 'BOOKING_CONFIRM/CASE_NODE/MATERIAL_AUDIT/RENEWAL_REMIND',
    payload       JSON         DEFAULT NULL,
    send_status   VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SENT/FAIL',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_openid (wx_openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订阅消息记录';

-- 初始管理员不在此插入：由应用首次启动 DataInitializer 以 bcrypt 现算写入（admin/admin123，上线必须改）
