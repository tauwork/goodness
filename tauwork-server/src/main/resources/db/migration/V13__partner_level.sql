-- 分销两级：直接客户、间接客户，以及提现。

ALTER TABLE partner
    ADD COLUMN parent_id BIGINT DEFAULT NULL COMMENT '上级合伙人，其客户计为上级的间接客户' AFTER code;

ALTER TABLE partner_rule
    ADD COLUMN rate_l2 DECIMAL(5,2) NOT NULL DEFAULT 3.00 COMMENT '间接提成比例 %' AFTER rate;

ALTER TABLE partner_commission
    ADD COLUMN level VARCHAR(16) NOT NULL DEFAULT 'DIRECT' COMMENT 'DIRECT直接 / INDIRECT间接 / OTHER其它' AFTER source,
    ADD COLUMN ref_commission_id BIGINT DEFAULT NULL COMMENT '间接提成对应的直接提成' AFTER level;

UPDATE partner_rule SET rate_l2 = 3.00 WHERE rate_l2 IS NULL;

CREATE TABLE partner_withdraw (
    id           BIGINT        NOT NULL,
    partner_id   BIGINT        NOT NULL,
    amount       DECIMAL(12,2) NOT NULL,
    status       VARCHAR(16)   NOT NULL COMMENT 'PENDING审核中 / PAID已提 / REJECTED驳回',
    remark       VARCHAR(255)  DEFAULT NULL,
    create_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by    BIGINT        DEFAULT NULL,
    update_by    BIGINT        DEFAULT NULL,
    deleted      TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_withdraw_partner (partner_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合伙人提现';
