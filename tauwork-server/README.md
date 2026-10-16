# tauwork-server · 后端服务

Spring Boot 3 + MyBatis-Plus + MySQL 8 + Sa-Token + SpringDoc(OpenAPI)

## 分层约定
- module/{业务域} 为业务模块目录，每个业务域内按 controller/service/mapper/entity/dto 四层
- 单体架构，禁用跨模块 service 互调，公共逻辑下沉 common；后续按域拆分时以 module 为单位迁移
- mapper XML 放 resources/mapper（与 Java mapper 同名）；建表脚本放 db/migration

## 业务域划分（对应 MVP）
customer 客户与家庭档案 | lead 线索池(CRM) | booking 咨询预约 | product 服务目录/题库
assessment 智能评估(engine=计分引擎) | order 订单 | caseflow 案件流程(engine=流程引擎)
material 材料清单与文件 | consultant 顾问排班 | message 订阅消息 | sysuser 后台账号权限
