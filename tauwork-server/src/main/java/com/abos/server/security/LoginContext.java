package com.abos.server.security;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 登录态解析：小程序客户登录ID格式为 "C{customerId}"，后台账号为裸ID
 */
public final class LoginContext {

    private LoginContext() {}

    /** 当前小程序客户ID（未登录抛 NotLoginException） */
    public static Long currentCustomerId() {
        String loginId = StpUtil.getLoginIdAsString();
        if (!loginId.startsWith("C")) {
            throw new IllegalStateException("当前会话不是小程序客户");
        }
        return Long.parseLong(loginId.substring(1));
    }

    /** 当前后台账号ID */
    public static Long currentAdminId() {
        return StpUtil.getLoginIdAsLong();
    }
}
