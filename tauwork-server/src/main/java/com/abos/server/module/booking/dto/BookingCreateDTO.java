package com.abos.server.module.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingCreateDTO {

    @NotNull(message = "communityProductId不能为空")
    private Long communityProductId;

    @NotNull(message = "bookDate不能为空")
    private LocalDate bookDate;

    /** HH:mm（HOUR 计价必填） */
    private String startTime;

    private Integer hours;

    /** DAY/MONTH/ONCE 计价数量，默认1 */
    private Integer qty;
}
