package com.abos.server.module.coreg.dto;

import lombok.Data;

import java.util.List;

@Data
public class CoRegKycDTO {
    private List<CoRegMemberDTO> members;
}
