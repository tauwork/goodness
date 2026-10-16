package com.abos.server.module.booking.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.booking.entity.SvcBooking;
import com.abos.server.module.booking.service.SvcBookingService;
import com.abos.server.security.LoginContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序服务预约（需登录）：
 *  POST /api/mini/svc-booking   提交预约（咨询主题/方式/日期/时段/意向服务）
 *  GET  /api/mini/svc-booking/my 我的预约
 */
@RestController
@RequestMapping("/api/mini/svc-booking")
@RequiredArgsConstructor
public class SvcBookingMiniController {

    private final SvcBookingService svcBookingService;

    @PostMapping
    public Result<SvcBooking> create(@Valid @RequestBody SvcBooking dto) {
        return Result.ok(svcBookingService.create(LoginContext.currentCustomerId(), dto));
    }

    @GetMapping("/my")
    public Result<List<SvcBooking>> my() {
        return Result.ok(svcBookingService.myList(LoginContext.currentCustomerId()));
    }
}
