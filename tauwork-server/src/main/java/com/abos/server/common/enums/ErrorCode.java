package com.abos.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码：按业务域分段
 * 0 成功 | 1xxx 通用 | 2xxx 认证 | 3xxx 客户/线索 | 4xxx 产品/评估 | 5xxx 订单 | 6xxx 案件 | 7xxx 材料
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "success"),

    BAD_REQUEST(1001, "参数错误"),
    NOT_FOUND(1002, "资源不存在"),
    OPERATION_FAILED(1003, "操作失败"),

    UNAUTHORIZED(2001, "未登录或登录已过期"),
    FORBIDDEN(2002, "无权限访问"),
    ACCOUNT_DISABLED(2003, "账号已停用"),

    LEAD_NOT_FOUND(3001, "线索不存在"),
    CUSTOMER_NOT_FOUND(3002, "客户不存在"),
    LEAD_ALREADY_ASSIGNED(3003, "线索已分配"),

    PRODUCT_NOT_FOUND(4001, "服务不存在"),
    PRODUCT_NOT_ON_SALE(4002, "服务已下架"),
    ASSESSMENT_NOT_FOUND(4003, "评估记录不存在"),

    ORDER_NOT_FOUND(5001, "订单不存在"),
    ORDER_STATUS_INVALID(5002, "订单状态不允许该操作"),

    CASE_NOT_FOUND(6001, "案件不存在"),
    CASE_NODE_INVALID(6002, "案件节点流转不合法"),

    MATERIAL_NOT_FOUND(7001, "材料项不存在"),
    MATERIAL_FILE_TOO_LARGE(7002, "文件超过大小限制");

    private final int code;
    private final String msg;
}
