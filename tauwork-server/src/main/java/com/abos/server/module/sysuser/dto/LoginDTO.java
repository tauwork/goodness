package com.abos.server.module.sysuser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Data
    public static class LoginVO {
        private String token;
        private Long userId;
        private String realName;
        private String roleCode;
    }
}
