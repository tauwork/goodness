package com.abos.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tauworkOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Tauwork 九千善数智平台 API")
                .description("小程序端 /api/mini/** · 后台端 /api/admin/**（角色权限见系统设置-权限矩阵）")
                .version("0.1.0"));
    }
}
