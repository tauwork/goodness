package com.abos.server.module.product.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConsultCatalogVO {

    private List<Group> groups = new ArrayList<>();
    private List<Item> services = new ArrayList<>();

    @Data
    public static class Group {
        private String code;
        private String name;
        private Contact consultant;
    }

    @Data
    public static class Item {
        private Long id;
        private String name;
        private String subtitle;
        private String group;
        private Contact consultant;
    }

    @Data
    public static class Contact {
        private String name;
        private String title;
        private String wechat;
        private String phone;
    }
}
