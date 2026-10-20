package com.abos.server.module.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 本地文件存储。dir 相对进程工作目录，url-prefix 为对外访问路径。
 */
@Data
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    /** 落盘目录，默认 data/uploads */
    private String dir = "data/uploads";

    /** 对外 URL 前缀，默认 /files */
    private String urlPrefix = "/files";
}
