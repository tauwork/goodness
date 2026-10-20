package com.abos.server.module.crm.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AppointmentCreateDTO {
    private Long customerId;
    private String name;
    private String phone;
    /** 手机号还没有档案时，用姓名建一份意向客户 */
    private Boolean createIfAbsent;
    private String visitType;
    private String topic;
    private LocalDate bookDate;
    private String timeSlot;
    private String place;
    private Long ownerId;
    private String remark;
}
