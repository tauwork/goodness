package com.abos.server.module.crm.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.crm.dto.AppointmentCreateDTO;
import com.abos.server.module.crm.entity.CrmAppointment;
import com.abos.server.module.crm.service.CrmAppointmentService;
import com.abos.server.security.LoginContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/appointment")
@RequiredArgsConstructor
public class CrmAppointmentAdminController {

    private final CrmAppointmentService appointmentService;

    @GetMapping("/list")
    public Result<List<CrmAppointment>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return Result.ok(appointmentService.list(bookDate, status, keyword));
    }

    @PostMapping
    @SaCheckPermission("crm:manage")
    public Result<CrmAppointment> create(@RequestBody AppointmentCreateDTO dto) {
        return Result.ok(appointmentService.create(dto, LoginContext.currentAdminId()));
    }

    @PutMapping("/{id}/status/{status}")
    @SaCheckPermission("crm:manage")
    public Result<CrmAppointment> status(@PathVariable Long id, @PathVariable String status) {
        return Result.ok(appointmentService.changeStatus(id, status));
    }
}
