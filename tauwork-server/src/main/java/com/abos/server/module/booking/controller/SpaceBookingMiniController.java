package com.abos.server.module.booking.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.booking.dto.BookingCreateDTO;
import com.abos.server.module.booking.entity.SpaceBooking;
import com.abos.server.module.booking.service.SpaceBookingService;
import com.abos.server.security.LoginContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 小程序空间预订（需登录）：
 *  POST /api/mini/booking            下单并返回微信支付参数
 *  GET  /api/mini/booking/my         我的预订（含入场码/状态）
 *  POST /api/mini/booking/mock-pay/{orderNo}  开发模式模拟支付成功
 */
@RestController
@RequestMapping("/api/mini/booking")
@RequiredArgsConstructor
public class SpaceBookingMiniController {

    private final SpaceBookingService bookingService;

    @PostMapping
    public Result<Map<String, Object>> create(@Valid @RequestBody BookingCreateDTO dto) {
        return Result.ok(bookingService.create(LoginContext.currentCustomerId(), dto));
    }

    @GetMapping("/my")
    public Result<List<SpaceBooking>> my() {
        return Result.ok(bookingService.myBookings(LoginContext.currentCustomerId()));
    }

    @PostMapping("/mock-pay/{orderNo}")
    public Result<Void> mockPay(@PathVariable String orderNo) {
        bookingService.mockPayConfirm(orderNo);
        return Result.ok();
    }
}
