package com.abos.server.module.svcorder.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VoucherSubmitDTO {

    @NotBlank(message = "请上传转账凭证")
    private String url;
}
