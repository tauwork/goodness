-- ============================================================
-- V0.6 智能评估：题库 + 计分规则 + 推荐路径
-- 设计：题目按题组(question_group)渲染；规则按选项映射分值；
--      推荐路径按条件表达式匹配（简化为 threshold 字段：总分达到则推荐）
-- ============================================================

-- 评估题库版本
CREATE TABLE assess_quiz (
    id         BIGINT      NOT NULL,
    version    VARCHAR(16) NOT NULL COMMENT '题库版本 v1/v2…',
    status     TINYINT     NOT NULL DEFAULT 1 COMMENT '1生效 0停用（同版本仅一个生效）',
    create_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by  BIGINT      DEFAULT NULL,
    update_by  BIGINT      DEFAULT NULL,
    deleted    TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_version (version, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估题库';

-- 题目（按 seq 顺序渲染，group_name 分组展示）
CREATE TABLE assess_question (
    id          BIGINT      NOT NULL,
    quiz_id     BIGINT      NOT NULL,
    seq         INT         NOT NULL,
    group_name  VARCHAR(32) NOT NULL COMMENT '题组：基础条件/收入/成就',
    code        VARCHAR(32) NOT NULL COMMENT '题目编码：edu/school/exp/income/achievement',
    title       VARCHAR(128) NOT NULL,
    multi       TINYINT     NOT NULL DEFAULT 0 COMMENT '0单选 1多选',
    weight      INT         NOT NULL DEFAULT 0 COMMENT '计分权重(满分贡献)',
    sort_no     INT         NOT NULL DEFAULT 0,
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   BIGINT      DEFAULT NULL,
    update_by   BIGINT      DEFAULT NULL,
    deleted     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_quiz (quiz_id, seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估题目';

-- 选项（label 展示；score 分值；tag 附加标签如 高意向）
CREATE TABLE assess_option (
    id          BIGINT      NOT NULL,
    question_id BIGINT      NOT NULL,
    seq         INT         NOT NULL,
    label       VARCHAR(64) NOT NULL,
    score       INT         NOT NULL DEFAULT 0,
    tag         VARCHAR(32) DEFAULT NULL COMMENT '命中标签：GaoCaiA/百强/高管…',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   BIGINT      DEFAULT NULL,
    update_by   BIGINT      DEFAULT NULL,
    deleted     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_question (question_id, seq)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估选项';

-- 推荐路径（threshold: 满足分数下限且命中标签即推荐；sort 越小优先级越高）
CREATE TABLE assess_recommend (
    id          BIGINT      NOT NULL,
    quiz_id     BIGINT      NOT NULL,
    code        VARCHAR(32) NOT NULL COMMENT 'GAOCAI_A/YOUCAI/HK_MASTER/CIES…',
    name        VARCHAR(64) NOT NULL COMMENT '展示名：高端人才通行证·高才A类',
    product_id  BIGINT      DEFAULT NULL COMMENT '关联服务产品',
    min_score   INT         NOT NULL DEFAULT 0 COMMENT '分数下限',
    require_tag VARCHAR(64) DEFAULT NULL COMMENT '须命中标签(可空)',
    priority    INT         NOT NULL DEFAULT 0 COMMENT '越小越优先',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by   BIGINT      DEFAULT NULL,
    update_by   BIGINT      DEFAULT NULL,
    deleted     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_quiz (quiz_id, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估推荐路径';

-- ========== 种子题库 v1（对齐原型 v1.1 的评估页 5 题） ==========
INSERT INTO assess_quiz (id, version, status) VALUES (1, 'v1', 1);

INSERT INTO assess_question (id, quiz_id, seq, group_name, code, title, multi, weight, sort_no) VALUES
(11,1,1,'基础条件','edu','最高学历',0,20,1),
(12,1,2,'基础条件','school','毕业院校层次',0,30,2),
(13,1,3,'基础条件','exp','工作年限',0,15,3),
(14,1,4,'收入','income','年收入区间（人民币）',0,35,4),
(15,1,5,'成就','achievement','主要成就（可多选）',1,10,5);

INSERT INTO assess_option (id, question_id, seq, label, score, tag) VALUES
(111,11,1,'博士',20,NULL),(112,11,2,'硕士',14,NULL),(113,11,3,'本科',8,NULL),(114,11,4,'其他',0,NULL),
(121,12,1,'全球百强名校',30,'TOP100'),(122,12,2,'985 / 211',18,NULL),(123,12,3,'普通院校',6,NULL),(124,12,4,'海外院校',10,NULL),
(131,13,1,'10年以上',15,NULL),(132,13,2,'3 - 10年',9,NULL),(133,13,3,'3年以内',3,NULL),
(141,14,1,'250万以上',35,'HIGH_INCOME'),(142,14,2,'100 - 250万',20,NULL),(143,14,3,'100万以下',5,NULL),
(151,15,1,'行业奖项 / 荣誉',4,'AWARD'),(152,15,2,'专利 / 著作',3,'PATENT'),(153,15,3,'企业主 / 合伙人',3,'FOUNDER'),(154,15,4,'名企高管',3,'EXEC');

INSERT INTO assess_recommend (id, quiz_id, code, name, product_id, min_score, require_tag, priority) VALUES
(21,1,'GAOCAI_A','高端人才通行证 · 高才 A类',2001,60,'HIGH_INCOME',1),
(22,1,'GAOCAI_BC','高端人才通行证 · 高才 B/C类',2001,70,'TOP100',2),
(23,1,'YOUCAI','优秀人才入境计划 · 优才',2002,55,NULL,3),
(24,1,'HK_MASTER','港硕进修 + 家属陪伴',1002,0,NULL,4);
