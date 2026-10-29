package com.abos.server.module.svcorder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SvcOrderCreateDTO {

    @NotNull(message = "请选择服务")
    private Long productId;

    /** WECHAT 在线支付 / TRANSFER 对公转账 */
    @NotNull(message = "请选择支付方式")
    private String payChannel;
}
