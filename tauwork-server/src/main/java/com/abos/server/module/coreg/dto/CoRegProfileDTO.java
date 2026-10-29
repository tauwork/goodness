package com.abos.server.module.coreg.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CoRegProfileDTO {
    private String bizScope;
    private BigDecimal capitalAmount;
    private List<CoRegMemberDTO> members;
}
