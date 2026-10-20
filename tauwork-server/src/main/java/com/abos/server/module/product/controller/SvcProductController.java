package com.abos.server.module.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.service.SvcProductService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品主数据（通用商品 / 服务项目 / 空间产品）
 */
@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class SvcProductController {

    private final SvcProductService productService;

    @GetMapping("/page")
    public Result<IPage<SvcProduct>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "10") long size,
                                          @RequestParam(required = false) String prodType,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) Integer onSale,
                                          @RequestParam(required = false) String keyword) {
        return Result.ok(productService.page(page, size, prodType, categoryId, onSale, keyword));
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

    @PutMapping("/{id}/on-sale")
    @SaCheckPermission("product:manage")
    public Result<Void> onSale(@PathVariable Long id) {
        productService.setOnSale(id, 1);
        return Result.ok();
    }

    @PutMapping("/{id}/off-sale")
    @SaCheckPermission("product:manage")
    public Result<Void> offSale(@PathVariable Long id) {
        productService.offSale(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("product:manage")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok();
    }
}
