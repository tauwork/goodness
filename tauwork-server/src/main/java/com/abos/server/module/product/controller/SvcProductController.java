package com.abos.server.module.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.service.SvcProductService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 服务产品目录（后台管理端 /api/admin/**；小程序端只读接口在 V0.2 增加）
 */
@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class SvcProductController {

    private final SvcProductService productService;

    @GetMapping("/page")
    public Result<IPage<SvcProduct>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String bizLine,
                                          @RequestParam(required = false) Integer onSale) {
        return Result.ok(productService.page(page, size, bizLine, onSale));
    }

    @GetMapping("/{id}")
    public Result<SvcProduct> detail(@PathVariable Long id) {
        return Result.ok(productService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("product:manage")
    public Result<SvcProduct> create(@RequestBody SvcProduct product) {
        return Result.ok(productService.create(product));
    }

    @PutMapping
    @SaCheckPermission("product:manage")
    public Result<SvcProduct> update(@RequestBody SvcProduct product) {
        return Result.ok(productService.update(product));
    }

    @PutMapping("/{id}/off-sale")
    @SaCheckPermission("product:manage")
    public Result<Void> offSale(@PathVariable Long id) {
        productService.offSale(id);
        return Result.ok();
    }
}
