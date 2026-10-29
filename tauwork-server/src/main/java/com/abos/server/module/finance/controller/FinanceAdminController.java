package com.abos.server.module.finance.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.finance.entity.DepositRecord;
import com.abos.server.module.finance.entity.FinanceAccount;
import com.abos.server.module.finance.entity.ReceivableBill;
import com.abos.server.module.finance.entity.RevenueSettlement;
import com.abos.server.module.finance.service.DepositService;
import com.abos.server.module.finance.service.FinanceAccountService;
import com.abos.server.module.finance.service.ReceivableService;
import com.abos.server.module.finance.service.SettlementService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/finance")
@RequiredArgsConstructor
public class FinanceAdminController {

    private final DepositService depositService;
    private final ReceivableService receivableService;
    private final SettlementService settlementService;
    private final FinanceAccountService financeAccountService;

    @GetMapping("/account")
    public Result<FinanceAccount> account() {
        return Result.ok(financeAccountService.get());
    }

    @PutMapping("/account")
    @SaCheckPermission("finance:manage")
    public Result<FinanceAccount> saveAccount(@RequestBody FinanceAccount account) {
        return Result.ok(financeAccountService.save(account));
    }

    @GetMapping("/deposit/list")
    public Result<List<DepositRecord>> deposits(@RequestParam(required = false) String kind,
                                                @RequestParam(required = false) String status) {
        return Result.ok(depositService.list(kind, status));
    }

    @PostMapping("/deposit")
    @SaCheckPermission("finance:manage")
    public Result<DepositRecord> createDeposit(@RequestBody DepositRecord deposit) {
        return Result.ok(depositService.create(deposit));
    }

    @PostMapping("/deposit/{id}/refund")
    @SaCheckPermission("finance:manage")
    public Result<DepositRecord> refund(@PathVariable Long id) {
        return Result.ok(depositService.refund(id));
    }

    @PostMapping("/deposit/{id}/apply")
    @SaCheckPermission("finance:manage")
    public Result<DepositRecord> apply(@PathVariable Long id) {
        return Result.ok(depositService.apply(id));
    }

    @DeleteMapping("/deposit/{id}")
    @SaCheckPermission("finance:manage")
    public Result<Void> deleteDeposit(@PathVariable Long id) {
        depositService.delete(id);
        return Result.ok();
    }

    @GetMapping("/receivable/list")
    public Result<List<ReceivableBill>> receivables(@RequestParam(required = false) String status) {
        return Result.ok(receivableService.list(status));
    }

    @PostMapping("/receivable")
    @SaCheckPermission("finance:manage")
    public Result<ReceivableBill> createReceivable(@RequestBody ReceivableBill bill) {
        return Result.ok(receivableService.create(bill));
    }

    @PostMapping("/receivable/{id}/receive")
    @SaCheckPermission("finance:manage")
    public Result<ReceivableBill> receive(@PathVariable Long id, @RequestBody MoneyBody body) {
        return Result.ok(receivableService.receive(id, body.getAmount()));
    }

    @PostMapping("/receivable/{id}/void")
    @SaCheckPermission("finance:manage")
    public Result<ReceivableBill> voidReceivable(@PathVariable Long id) {
        return Result.ok(receivableService.voidBill(id));
    }

    @DeleteMapping("/receivable/{id}")
    @SaCheckPermission("finance:manage")
    public Result<Void> deleteReceivable(@PathVariable Long id) {
        receivableService.delete(id);
        return Result.ok();
    }

    @GetMapping("/settlement/list")
    public Result<List<RevenueSettlement>> settlements(@RequestParam(required = false) String status) {
        return Result.ok(settlementService.list(status));
    }

    @PostMapping("/settlement")
    @SaCheckPermission("finance:manage")
    public Result<RevenueSettlement> createSettlement(@RequestBody RevenueSettlement settlement) {
        return Result.ok(settlementService.create(settlement));
    }

    @PostMapping("/settlement/{id}/void")
    @SaCheckPermission("finance:manage")
    public Result<RevenueSettlement> voidSettlement(@PathVariable Long id) {
        return Result.ok(settlementService.voidSettlement(id));
    }

    @Data
    public static class MoneyBody {
        private BigDecimal amount;
    }
}
