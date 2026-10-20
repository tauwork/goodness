package com.abos.server.module.customer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.customer.dto.CustomerDetailVO;
import com.abos.server.module.customer.dto.CustomerProfileDTO;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.entity.FamilyMember;
import com.abos.server.module.customer.service.CustomerAdminService;
import com.abos.server.security.LoginContext;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/customer")
@RequiredArgsConstructor
public class CustomerAdminController {

    private final CustomerAdminService customerAdmin;

    @GetMapping("/page")
    public Result<IPage<Customer>> page(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "20") long size,
                                        @RequestParam(required = false) String stage,
                                        @RequestParam(required = false) String keyword) {
        return Result.ok(customerAdmin.page(page, size, stage, keyword));
    }

    @GetMapping("/by-phone")
    public Result<Customer> byPhone(@RequestParam String phone) {
        return Result.ok(customerAdmin.findByPhone(phone));
    }

    @GetMapping("/{id}")
    public Result<CustomerDetailVO> detail(@PathVariable Long id) {
        return Result.ok(customerAdmin.detail(id));
    }

    @PostMapping
    @SaCheckPermission("crm:manage")
    public Result<Customer> create(@RequestBody CustomerProfileDTO dto) {
        return Result.ok(customerAdmin.create(dto, null));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("crm:manage")
    public Result<Customer> update(@PathVariable Long id, @RequestBody CustomerProfileDTO dto) {
        return Result.ok(customerAdmin.update(id, dto));
    }

    @PutMapping("/{id}/claim")
    @SaCheckPermission("crm:manage")
    public Result<Customer> claim(@PathVariable Long id) {
        return Result.ok(customerAdmin.claim(id, LoginContext.currentAdminId()));
    }

    @PutMapping("/{id}/release")
    @SaCheckPermission("crm:manage")
    public Result<Customer> release(@PathVariable Long id) {
        return Result.ok(customerAdmin.release(id));
    }

    @PutMapping("/{id}/stage/{stage}")
    @SaCheckPermission("crm:manage")
    public Result<Customer> stage(@PathVariable Long id, @PathVariable String stage) {
        return Result.ok(customerAdmin.changeStage(id, stage, LoginContext.currentAdminId()));
    }

    @PostMapping("/{id}/family")
    @SaCheckPermission("crm:manage")
    public Result<FamilyMember> saveFamily(@PathVariable Long id, @RequestBody FamilyMember member) {
        return Result.ok(customerAdmin.saveFamily(id, member));
    }

    @DeleteMapping("/{id}/family/{memberId}")
    @SaCheckPermission("crm:manage")
    public Result<Void> deleteFamily(@PathVariable Long id, @PathVariable Long memberId) {
        customerAdmin.deleteFamily(id, memberId);
        return Result.ok();
    }
}
