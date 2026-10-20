package com.abos.server.module.file.dto;

import lombok.Data;

@Data
public class StoredFile {

    /** 可直接用于 img src 的地址，如 /files/20261019/xxx.jpg */
    private String url;
    private String originalName;
    private long size;
    private String contentType;
}
