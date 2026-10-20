package com.abos.server.module.sysuser.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuNode {
    private Long id;
    private String title;
    private String path;
    private String icon;
    private String menuType;
    private String perm;
    private List<MenuNode> children = new ArrayList<>();
}
