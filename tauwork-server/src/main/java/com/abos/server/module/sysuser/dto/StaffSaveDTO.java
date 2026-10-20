package com.abos.server.module.sysuser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class StaffSaveDTO {
    private Long id;
    @NotBlank(message = "请填写登录名")
    private String username;
    private String password;
    @NotBlank(message = "请填写姓名")
    private String realName;
    private String phone;
    @NotBlank(message = "请选择角色")
    private String roleCode;
    private Integer status;
    private List<Long> communityIds;
}
