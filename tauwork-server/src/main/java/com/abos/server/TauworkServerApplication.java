package com.abos.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Tauwork 九千善数智平台后端服务
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.abos.server.module.**.mapper")
public class TauworkServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TauworkServerApplication.class, args);
    }
}
