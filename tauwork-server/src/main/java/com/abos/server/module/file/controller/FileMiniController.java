package com.abos.server.module.file.controller;

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
 * 小程序客户上传，用于转账凭证。需登录。
 */
@RestController
@RequestMapping("/api/mini/file")
@RequiredArgsConstructor
public class FileMiniController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public Result<StoredFile> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(fileStorageService.store(file));
    }
}
