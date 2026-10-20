package com.abos.server.module.booking.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SvcBookStaffDTO {
    private Long customerId;
    private String phone;
    private String name;
    private Boolean createIfAbsent;
    private Long productId;
    private String mode;
    private String topic;
    private LocalDate bookDate;
    private String timeSlot;
    private String remark;
}
