package com.abos.server.module.coreg.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoRegMemberDTO {
    private Long id;
    private String name;
    private String role;
    private BigDecimal shareRatio;
    private String idFrontUrl;
    private String idBackUrl;
    private String idHoldUrl;
}
