-- ============================================================
-- V0.5 服务产品清单初始化（对齐《九千善 SKU 交付清单与执行SOP v3.0》25 SKU）
-- 四大业务线：EDU 教育(E1-E8) / IDENTITY 身份(I1-I6) / STARTUP 创业(B1-B6) / LIVING 安居(A1-A5)
-- 价格为建议内部价（人民币），代收代付单列不计入服务费
-- ============================================================

-- 板块分类调整：原"其他服务/续签与永居"并入新四板块结构
UPDATE product_category SET name='香港教育', code='EDU' WHERE id=103;
DELETE FROM product_category WHERE id IN (104, 105);
INSERT INTO product_category (id, prod_type, code, name, icon, sort_no) VALUES
(104,'SERVICE','LIVING','香港安居','home',4);

-- 服务产品全量初始化：先清旧服务 SKU（保留空间产品 5001-5007），再按 25 SKU 插入
DELETE FROM svc_product WHERE prod_type = 'SERVICE' OR prod_type IS NULL;

INSERT INTO svc_product (id, prod_type, category_id, name, subtitle, price_mode, price_value, biz_line, on_sale, sort_no) VALUES
-- 一 · 香港教育 E1-E8
(1001,'SERVICE',103,'DSE 备考诊断包','学情测评+2027 DSE报考指导+湾区考场资讯，可抵全年班学费','FIXED',1000,'EDU',1,11),
(1002,'SERVICE',103,'升学规划指导','1对1中长期陪跑（1-3年）：选科指导+职业规划+备考路径管理','FIXED',6000,'EDU',1,12),
(1003,'SERVICE',103,'DSE 单科强化班','单科精讲+模考+错题诊断，按学期计费','FIXED',29800,'EDU',1,13),
(1004,'SERVICE',103,'DSE 半年提分班','核心6科系统课+估分系统+家校反馈','FIXED',79800,'EDU',1,14),
(1005,'SERVICE',103,'DSE 全年保驾班','高端全年班，目标名校升学陪跑','FIXED',138000,'EDU',1,15),
(1006,'SERVICE',103,'香港中小学插班','全链路插班（含身份通道核验）','FIXED',48000,'EDU',1,16),
(1007,'SERVICE',103,'香港中小学面试','面试单点辅导，可单独报名','FIXED',20000,'EDU',1,17),
(1008,'SERVICE',103,'国际学校升学规划','国际课程/海外方向综合规划','FIXED',88000,'EDU',1,18),
-- 二 · 香港身份 I1-I6
(2001,'SERVICE',101,'高端人才通行证（高才通 TTPS）','A/B/C类全程办理，官方网上递交+进度跟踪','FIXED',68000,'IDENTITY',1,21),
(2002,'SERVICE',101,'优秀人才入境计划（优才 QMAS）','12项评核准则综合计分制申报','FIXED',58000,'IDENTITY',1,22),
(2003,'SERVICE',101,'输入内地人才计划（专才 ASMTP）','雇主+申请人材料分列申报','FIXED',52000,'IDENTITY',1,23),
(2004,'SERVICE',101,'IANG 留港/回港就业','港校毕业生就业安排申请','FIXED',28000,'IDENTITY',1,24),
(2005,'SERVICE',101,'新资本投资者入境计划（CIES）','净资产审查→原则批准→投资审查→正式批准（不含3000万港元投资款）','FIXED',298000,'IDENTITY',1,25),
(2006,'SERVICE',101,'续签托管/永居规划','按次计费，存量续签与永居路径设计','FIXED',36000,'IDENTITY',1,26),
-- 三 · 香港创业 B1-B6
(3001,'SERVICE',102,'香港公司注册','注册处登记+BR，交付CI/章程/NNC1','FIXED',6800,'STARTUP',1,31),
(3002,'SERVICE',102,'银行开户 KYC 包','真实业务证明+材料预审+开户协助（不承诺100%下户）','FIXED',8800,'STARTUP',1,32),
(3003,'SERVICE',102,'代理记账报税','月结+年度利得税申报，按年计费','FIXED',9800,'STARTUP',1,33),
(3004,'SERVICE',102,'年度审计','财务报表审计+呈交，按年计费','FIXED',12800,'STARTUP',1,34),
(3005,'SERVICE',102,'财税合规规划','高才/优才自雇人群合规规划，按复杂度报价','STAGED',15000,'STARTUP',1,35),
(3006,'SERVICE',102,'创业全链路包','注册+开户+记账+合规打包，与身份自雇续签联动','FIXED',28000,'STARTUP',1,36),
-- 四 · 香港安居 A1-A5
(4001,'SERVICE',104,'香港租房安家服务','落地第一站，新来港家庭','FIXED',12800,'LIVING',1,41),
(4002,'SERVICE',104,'校网择校与学区安居规划','教育+安居联动，子女入学家庭','FIXED',9800,'LIVING',1,42),
(4003,'SERVICE',104,'家庭落地安顿包','生活基础设施，初到港家庭','FIXED',6800,'LIVING',1,43),
(4004,'SERVICE',104,'医疗与保险配置咨询','风险兜底，全家在港家庭','FIXED',8800,'LIVING',1,44),
(4005,'SERVICE',104,'全家安居全链路包','租房+择校+安顿+医疗打包','FIXED',38000,'LIVING',1,45);

-- 预置流程模板（身份通道 SOP，供后续案件引擎使用；官方口径访问 2026-10-10）
DELETE FROM flow_template_node WHERE tpl_id IN (SELECT id FROM flow_template);
DELETE FROM flow_template;

INSERT INTO flow_template (id, biz_type, name, node_count) VALUES
(501,'GAOCAI_TTPS','高才通 A/B/C 类',6),
(502,'QMAS','优才综合计分制',6),
(503,'ASMTP','专才计划',5),
(504,'IANG','IANG 就业安排',4),
(505,'CIES','新资本投资者入境计划',6);

INSERT INTO flow_template_node (id, tpl_id, seq, name, role_code, sla_days) VALUES
(50101,501,1,'类别判定（A/B/C）','CONSULTANT',2),
(50102,501,2,'学历核验与第三方认证','CASE_OPS',10),
(50103,501,3,'材料制备','CASE_OPS',14),
(50104,501,4,'网上递交（缴申请费 HK$600）','CASE_OPS',1),
(50105,501,5,'补件与进度跟进','CASE_OPS',30),
(50106,501,6,'获批·电子签证激活','CONSULTANT',7),
(50201,502,1,'12项评核准则评分','CONSULTANT',3),
(50202,502,2,'成就证明材料制备','CASE_OPS',14),
(50203,502,3,'网上递交','CASE_OPS',1),
(50204,502,4,'补件与跟进','CASE_OPS',45),
(50205,502,5,'获批·签证激活','CONSULTANT',7),
(50206,502,6,'受养人同步办理','CASE_OPS',14);

-- 服务预约表：小程序提交咨询/服务预约，后台确认→完成→归因
CREATE TABLE svc_booking (
    id            BIGINT       NOT NULL,
    customer_id   BIGINT       NOT NULL,
    product_id    BIGINT       DEFAULT NULL COMMENT '意向服务（可空=综合咨询）',
    product_name  VARCHAR(128) DEFAULT NULL COMMENT '冗余',
    topic         VARCHAR(64)  NOT NULL COMMENT '咨询主题',
    mode          VARCHAR(32)  NOT NULL COMMENT 'VIDEO/FUTIAN/BAOAN/KWAI_TSING',
    book_date     DATE         NOT NULL,
    time_slot     VARCHAR(8)   NOT NULL COMMENT 'HH:mm',
    status        VARCHAR(24)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING待确认/CONFIRMED已确认/DONE已完成/CANCELED已取消/NO_SHOW爽约',
    remark        VARCHAR(512) DEFAULT NULL COMMENT '客户补充说明',
    fail_reason   VARCHAR(64)  DEFAULT NULL COMMENT '未转化归因（完成时填写）',
    consultant_id BIGINT       DEFAULT NULL COMMENT '分配顾问',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by     BIGINT       DEFAULT NULL,
    update_by     BIGINT       DEFAULT NULL,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_customer (customer_id),
    KEY idx_date (book_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务咨询预约';

-- 时段字典（预约可选时段，后台可扩展）
CREATE TABLE booking_slot (
    id   BIGINT     NOT NULL,
    slot VARCHAR(8) NOT NULL COMMENT 'HH:mm',
    sort_no INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_slot (slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约时段';

INSERT INTO booking_slot (id, slot, sort_no) VALUES
(1,'09:30',1),(2,'10:30',2),(3,'14:00',3),(4,'15:30',4),(5,'16:30',5),(6,'19:00',6);
