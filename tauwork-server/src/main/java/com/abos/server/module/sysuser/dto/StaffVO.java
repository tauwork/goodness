package com.abos.server.module.sysuser.dto;

import lombok.Data;

import java.util.List;

@Data
public class StaffVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String roleCode;
    private String roleName;
    private Integer status;
    private List<Long> communityIds;
    private String communityNames;
}
