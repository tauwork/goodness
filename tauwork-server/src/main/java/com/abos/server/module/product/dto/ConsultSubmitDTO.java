package com.abos.server.module.product.dto;

import lombok.Data;

@Data
public class ConsultSubmitDTO {

    /** 服务大类编码，如 ALL / IDENTITY / COMPANY */
    private String group;
    private Long productId;
    private String name;
    private String phone;
    private String remark;
}
