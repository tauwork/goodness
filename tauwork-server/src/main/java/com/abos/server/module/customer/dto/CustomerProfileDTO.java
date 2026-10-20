package com.abos.server.module.customer.dto;

import lombok.Data;

@Data
public class CustomerProfileDTO {
    private String name;
    private String phone;
    private String gender;
    private String city;
    private String wechat;
    private String company;
    private String address;
    private String source;
    private String familyNote;
    private String remark;
}
