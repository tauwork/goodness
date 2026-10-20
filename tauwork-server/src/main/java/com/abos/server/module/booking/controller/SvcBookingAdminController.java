package com.abos.server.module.booking.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.booking.dto.SvcBookStaffDTO;
import com.abos.server.module.booking.entity.SvcBooking;
import com.abos.server.module.booking.service.SvcBookingService;
import com.abos.server.security.LoginContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 后台服务预约调度（对应运营平台「咨询预约调度」页）
 */
@RestController
@RequestMapping("/api/admin/svc-booking")
@RequiredArgsConstructor
public class SvcBookingAdminController {

    private final SvcBookingService svcBookingService;

    @GetMapping("/list")
    public Result<List<SvcBooking>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return Result.ok(svcBookingService.adminList(bookDate, status, keyword));
    }

    @PostMapping
    @SaCheckPermission("booking:manage")
    public Result<SvcBooking> create(@RequestBody SvcBookStaffDTO dto) {
        return Result.ok(svcBookingService.createByStaff(dto, LoginContext.currentAdminId()));
    }

    /** status: CONFIRMED/DONE/CANCELED/NO_SHOW（后两者可带 failReason 归因） */
    @PutMapping("/{id}/status/{status}")
    @SaCheckPermission("booking:manage")
    public Result<SvcBooking> changeStatus(@PathVariable Long id, @PathVariable String status,
                                           @RequestParam(required = false) String failReason) {
        return Result.ok(svcBookingService.changeStatus(id, status, failReason));
    }
}
