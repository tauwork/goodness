-- 客户档案扩展，手机号作为未删除客户的唯一标识
-- 阶段：POOL公海 / INTENT意向 / SIGNED签约 / REPEAT复购 / BLACKLIST黑名单

ALTER TABLE customer
    ADD COLUMN gender VARCHAR(8) NULL COMMENT 'M男/F女' AFTER phone_hash,
    ADD COLUMN city VARCHAR(64) NULL COMMENT '城市' AFTER source,
    ADD COLUMN wechat VARCHAR(64) NULL COMMENT '微信号' AFTER city,
    ADD COLUMN company VARCHAR(128) NULL COMMENT '公司或职业' AFTER wechat,
    ADD COLUMN address VARCHAR(255) NULL COMMENT '联系地址' AFTER company,
    ADD COLUMN remark VARCHAR(1000) NULL COMMENT '档案备注' AFTER family_note,
    ADD COLUMN next_follow_at DATETIME NULL COMMENT '下次跟进时间' AFTER remark;

UPDATE customer
SET stage = CASE
    WHEN stage = 'SIGNED' THEN 'SIGNED'
    WHEN stage = 'RENEWING' THEN 'REPEAT'
    WHEN stage = 'LOST' THEN 'POOL'
    WHEN owner_id IS NOT NULL THEN 'INTENT'
    ELSE 'POOL'
END;

ALTER TABLE customer
    ALTER COLUMN stage SET DEFAULT 'POOL',
    ADD COLUMN phone_uk VARCHAR(20)
        GENERATED ALWAYS AS (IF(deleted = 0 AND phone IS NOT NULL AND phone <> '', phone, NULL)) STORED,
    ADD UNIQUE KEY uk_customer_phone (phone_uk);

CREATE TABLE crm_appointment (
    id            BIGINT       NOT NULL,
    customer_id   BIGINT       NOT NULL,
    visit_type    VARCHAR(16)  NOT NULL COMMENT 'VISIT上门/STORE到店/VIDEO视频/PHONE电话/MEETING会议',
    topic         VARCHAR(128) NOT NULL COMMENT '预约主题',
    book_date     DATE         NOT NULL,
    time_slot     VARCHAR(16)  NOT NULL COMMENT 'HH:mm',
    place         VARCHAR(128) DEFAULT NULL COMMENT '地点',
    owner_id      BIGINT       DEFAULT NULL COMMENT '接待人',
    status        VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/CONFIRMED/DONE/CANCELED/NO_SHOW',
    remark        VARCHAR(512) DEFAULT NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_customer (customer_id),
    KEY idx_date (book_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约登记';

CREATE TABLE crm_follow (
    id              BIGINT       NOT NULL,
    customer_id     BIGINT       NOT NULL,
    lead_id         BIGINT       DEFAULT NULL,
    appointment_id  BIGINT       DEFAULT NULL,
    follow_type     VARCHAR(16)  NOT NULL COMMENT 'VISIT拜访/MEETING会议/CALL电话/WECHAT微信/OTHER其他',
    content         VARCHAR(2000) NOT NULL,
    follow_at       DATETIME     NOT NULL,
    next_follow_at  DATETIME     DEFAULT NULL,
    owner_id        BIGINT       DEFAULT NULL COMMENT '记录人',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT       DEFAULT NULL,
    update_by       BIGINT       DEFAULT NULL,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_customer (customer_id, follow_at),
    KEY idx_lead (lead_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跟进日志';
