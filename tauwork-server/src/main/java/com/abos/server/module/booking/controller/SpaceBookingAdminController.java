package com.abos.server.module.booking.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.booking.entity.SpaceBooking;
import com.abos.server.module.booking.service.SpaceBookingService;
import com.abos.server.security.LoginContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 后台：空间预订订单管理（对应运营平台「空间运营-今日预订」）
 * 运营可更改订单状态：USED 已使用 / CANCELED 已退订（已支付自动走退款）
 */
@RestController
@RequestMapping("/api/admin/booking")
@RequiredArgsConstructor
public class SpaceBookingAdminController {

    private final SpaceBookingService bookingService;

    @GetMapping("/list")
    public Result<List<SpaceBooking>> list(
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookDate,
            @RequestParam(required = false) String status) {
        return Result.ok(bookingService.adminList(communityId, bookDate, status));
    }

    @PutMapping("/{id}/status/{status}")
    @SaCheckPermission("space:manage")
    public Result<SpaceBooking> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return Result.ok(bookingService.changeStatus(id, status, LoginContext.currentAdminId()));
    }
}
