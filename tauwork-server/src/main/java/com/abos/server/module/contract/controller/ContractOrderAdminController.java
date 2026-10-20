package com.abos.server.module.contract.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.contract.entity.ContractOrder;
import com.abos.server.module.contract.service.ContractOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/contract")
@RequiredArgsConstructor
public class ContractOrderAdminController {

    private final ContractOrderService contractOrderService;

    @GetMapping("/list")
    public Result<List<ContractOrder>> list() {
        return Result.ok(contractOrderService.list());
    }

    @PostMapping
    @SaCheckPermission("contract:manage")
    public Result<ContractOrder> create(@RequestBody ContractOrder contract) {
        return Result.ok(contractOrderService.create(contract));
    }

    @PutMapping
    @SaCheckPermission("contract:manage")
    public Result<ContractOrder> update(@RequestBody ContractOrder contract) {
        return Result.ok(contractOrderService.update(contract));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("contract:manage")
    public Result<Void> delete(@PathVariable Long id) {
        contractOrderService.delete(id);
        return Result.ok();
    }
}
