package com.abos.server.module.file.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.file.config.UploadProperties;
import com.abos.server.module.file.dto.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 通用上传：文件只写到本机目录，文件名由服务端生成，避免路径穿越。
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final long MAX_BYTES = 10L * 1024 * 1024;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Map<String, String> TYPE_EXT = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/gif", "gif",
            "image/webp", "webp",
            "application/pdf", "pdf"
    );
    private static final Set<String> EXTS = Set.of("jpg", "jpeg", "png", "gif", "webp", "pdf");

    private final UploadProperties properties;

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择文件");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BizException(ErrorCode.MATERIAL_FILE_TOO_LARGE, "单文件不超过 10MB");
        }
        String ext = extension(file);
        String day = LocalDate.now().format(DAY);
        String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path root = Path.of(properties.getDir()).toAbsolutePath().normalize();
        Path target = root.resolve(day).resolve(name).normalize();
        if (!target.startsWith(root)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "非法文件路径");
        }
        try {
            Files.createDirectories(target.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target);
            }
        } catch (IOException e) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "文件保存失败");
        }
        String prefix = properties.getUrlPrefix();
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        StoredFile stored = new StoredFile();
        stored.setUrl(prefix + "/" + day + "/" + name);
        stored.setOriginalName(safeOriginalName(file.getOriginalFilename()));
        stored.setSize(file.getSize());
        stored.setContentType(file.getContentType());
        return stored;
    }

    private String extension(MultipartFile file) {
        String type = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String byType = TYPE_EXT.get(type);
        if (byType != null) {
            return byType;
        }
        String original = file.getOriginalFilename();
        if (StringUtils.hasText(original) && original.contains(".")) {
            String ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            if (EXTS.contains(ext)) {
                return "jpeg".equals(ext) ? "jpg" : ext;
            }
        }
        throw new BizException(ErrorCode.BAD_REQUEST, "只支持 jpg、png、gif、webp、pdf");
    }

    private String safeOriginalName(String name) {
        if (!StringUtils.hasText(name)) {
            return "file";
        }
        String base = name.replace("\\", "/");
        int slash = base.lastIndexOf('/');
        return slash >= 0 ? base.substring(slash + 1) : base;
    }
}
