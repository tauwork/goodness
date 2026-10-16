-- ============================================================
-- V0.4 微信登录 + 空间预订 + 订单支付
-- space_booking：空间预订单（关联 orders SP 单）
-- pay_log：微信支付流水（预支付/回调/退款）
-- ============================================================

-- 预订单状态补充（orders.pay_status 已有 UNPAID/PAID/INSTALLMENT/REFUNDED）
ALTER TABLE orders
    ADD COLUMN openid VARCHAR(64) DEFAULT NULL COMMENT '下单客户openid（冗余便查）' AFTER customer_id;

CREATE TABLE space_booking (
    id                    BIGINT        NOT NULL,
    order_id              BIGINT        NOT NULL COMMENT '关联 orders.id（SP单）',
    order_no              VARCHAR(32)   NOT NULL COMMENT '订单号冗余',
    customer_id           BIGINT        NOT NULL,
    community_id          BIGINT        NOT NULL COMMENT '社区店',
    community_product_id  BIGINT        NOT NULL COMMENT '社区产品关联id',
    book_date             DATE          NOT NULL COMMENT '预订日期',
    start_time            VARCHAR(8)    DEFAULT NULL COMMENT '开始时段 HH:mm（HOUR计价必填）',
    hours                 INT           DEFAULT NULL COMMENT '时长（HOUR计价）',
    qty                   INT           DEFAULT NULL COMMENT '数量（DAY/MONTH/ONCE 计价，默认1）',
    amount                DECIMAL(12,2) NOT NULL COMMENT '应收金额',
    status                VARCHAR(24)   NOT NULL DEFAULT 'PENDING_PAY' COMMENT 'PENDING_PAY待支付/PAID已支付/USED已使用/CANCELED已退订(退款)',
    entry_code            VARCHAR(16)   DEFAULT NULL COMMENT '入场码（支付成功后生成）',
    used_at               DATETIME      DEFAULT NULL COMMENT '核销时间',
    create_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by             BIGINT        DEFAULT NULL,
    update_by             BIGINT        DEFAULT NULL,
    deleted               TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order (order_id),
    UNIQUE KEY uk_entry (entry_code, deleted),
    KEY idx_customer (customer_id, book_date),
    KEY idx_community (community_id, book_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='空间预订单';

-- 微信支付流水
CREATE TABLE pay_log (
    id              BIGINT       NOT NULL,
    order_no        VARCHAR(32)  NOT NULL,
    prepay_id       VARCHAR(64)  DEFAULT NULL COMMENT '微信预支付ID',
    transaction_id  VARCHAR(64)  DEFAULT NULL COMMENT '微信支付单号',
    event           VARCHAR(24)  NOT NULL COMMENT 'PREPAY预支付/NOTIFY支付回调/REFUND退款',
    success         TINYINT      NOT NULL DEFAULT 0,
    raw             JSON         DEFAULT NULL COMMENT '原始报文/参数（排障用）',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT       DEFAULT NULL,
    update_by       BIGINT       DEFAULT NULL,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_order (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水';
