package com.abos.server.module.goods.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.goods.entity.GoodsOrder;
import com.abos.server.module.goods.service.GoodsOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台商品订单：交付 / 取消退款
 */
@RestController
@RequestMapping("/api/admin/goods-order")
@RequiredArgsConstructor
public class GoodsOrderAdminController {

    private final GoodsOrderService goodsOrderService;

    @GetMapping("/list")
    public Result<List<GoodsOrder>> list(@RequestParam(required = false) String status) {
        return Result.ok(goodsOrderService.adminList(status));
    }

    @PutMapping("/{id}/status/{status}")
    @SaCheckPermission("product:manage")
    public Result<GoodsOrder> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return Result.ok(goodsOrderService.changeStatus(id, status));
    }
}
