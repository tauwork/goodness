package com.abos.server.module.goods.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoodsOrderCreateDTO {

    @NotNull
    private Long productId;

    @NotNull
    @Min(1)
    @Max(99)
    private Integer qty;

    private String remark;
}
