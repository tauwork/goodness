package com.abos.server.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 认证拦截：
 * /api/** 全部要求登录；文档与探活接口放行。
 * 权限粒度（菜单/按钮级）由 @SaCheckPermission 注解在各 Controller 声明，见 sysuser 权限矩阵。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/mini/auth/login",
                        "/api/mini/cms/**",
                        "/api/mini/svc/**",
                        "/api/mini/space/**",
                        "/api/pay/notify/**",
                        "/v3/api-docs/**",
                        "/doc.html",
                        "/swagger-ui/**",
                        "/webjars/**",
                        "/actuator/health",
                        "/error"
                );
    }
}
