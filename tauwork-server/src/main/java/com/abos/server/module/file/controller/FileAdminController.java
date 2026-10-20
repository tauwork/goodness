package com.abos.server.module.file.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.file.dto.StoredFile;
import com.abos.server.module.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用文件上传。业务模块只保存返回的 url，不各自接存储。
 */
@RestController
@RequestMapping("/api/admin/file")
@RequiredArgsConstructor
public class FileAdminController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @SaCheckPermission("product:manage")
    public Result<StoredFile> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(fileStorageService.store(file));
    }
}
