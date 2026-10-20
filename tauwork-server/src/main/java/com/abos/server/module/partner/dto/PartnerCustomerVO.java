package com.abos.server.module.partner.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PartnerCustomerVO {
    private Long id;
    private String name;
    private String phone;
    private String partnerBindSource;
    private LocalDateTime partnerBindAt;
    /** DIRECT 直接 / INDIRECT 间接 */
    private String relation;
}
