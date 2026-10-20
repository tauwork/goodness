package com.abos.server.module.goods.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.goods.dto.GoodsOrderCreateDTO;
import com.abos.server.module.goods.entity.GoodsOrder;
import com.abos.server.module.goods.service.GoodsOrderService;
import com.abos.server.security.LoginContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 小程序通用商品下单（需登录）
 */
@RestController
@RequestMapping("/api/mini/goods-order")
@RequiredArgsConstructor
public class GoodsOrderMiniController {

    private final GoodsOrderService goodsOrderService;

    @PostMapping
    public Result<Map<String, Object>> create(@Valid @RequestBody GoodsOrderCreateDTO dto) {
        return Result.ok(goodsOrderService.create(LoginContext.currentCustomerId(), dto));
    }

    @PostMapping("/mock-pay/{orderNo}")
    public Result<Void> mockPay(@PathVariable String orderNo) {
        goodsOrderService.mockPay(LoginContext.currentCustomerId(), orderNo);
        return Result.ok();
    }

    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        goodsOrderService.cancelByCustomer(LoginContext.currentCustomerId(), orderNo);
        return Result.ok();
    }

    @GetMapping("/my")
    public Result<List<GoodsOrder>> my() {
        return Result.ok(goodsOrderService.myOrders(LoginContext.currentCustomerId()));
    }
}
