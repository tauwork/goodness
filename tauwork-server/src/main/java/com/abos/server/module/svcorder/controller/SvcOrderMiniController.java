package com.abos.server.module.svcorder.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.svcorder.dto.SvcOrderCreateDTO;
import com.abos.server.module.svcorder.dto.VoucherSubmitDTO;
import com.abos.server.module.svcorder.entity.SvcOrder;
import com.abos.server.module.svcorder.service.SvcOrderService;
import com.abos.server.security.LoginContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 小程序服务下单（需登录）。路径不放在 /api/mini/svc 下，避免被公开接口放行。
 */
@RestController
@RequestMapping("/api/mini/svc-order")
@RequiredArgsConstructor
public class SvcOrderMiniController {

    private final SvcOrderService svcOrderService;

    @PostMapping
    public Result<Map<String, Object>> create(@Valid @RequestBody SvcOrderCreateDTO dto) {
        return Result.ok(svcOrderService.create(LoginContext.currentCustomerId(), dto));
    }

    @PostMapping("/{orderNo}/pay")
    public Result<Map<String, Object>> pay(@PathVariable String orderNo) {
        return Result.ok(svcOrderService.repay(LoginContext.currentCustomerId(), orderNo));
    }

    @PostMapping("/mock-pay/{orderNo}")
    public Result<Void> mockPay(@PathVariable String orderNo) {
        svcOrderService.mockPay(LoginContext.currentCustomerId(), orderNo);
        return Result.ok();
    }

    @PostMapping("/{orderNo}/voucher")
    public Result<SvcOrder> voucher(@PathVariable String orderNo, @Valid @RequestBody VoucherSubmitDTO dto) {
        return Result.ok(svcOrderService.submitVoucher(LoginContext.currentCustomerId(), orderNo, dto.getUrl()));
    }

    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        svcOrderService.cancelByCustomer(LoginContext.currentCustomerId(), orderNo);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<List<SvcOrder>> my() {
        return Result.ok(svcOrderService.myOrders(LoginContext.currentCustomerId()));
    }

    @GetMapping("/{orderNo}")
    public Result<Map<String, Object>> one(@PathVariable String orderNo) {
        return Result.ok(svcOrderService.mine(LoginContext.currentCustomerId(), orderNo));
    }
}
