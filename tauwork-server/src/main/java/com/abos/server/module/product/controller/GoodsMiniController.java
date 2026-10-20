package com.abos.server.module.product.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.product.entity.ProductCategory;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.service.ProductCategoryService;
import com.abos.server.module.product.service.SvcProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序「通用商品」公开目录
 */
@RestController
@RequestMapping("/api/mini/goods")
@RequiredArgsConstructor
public class GoodsMiniController {

    private final SvcProductService productService;
    private final ProductCategoryService categoryService;

    @GetMapping("/category")
    public Result<List<ProductCategory>> categories() {
        return Result.ok(categoryService.listByType(SvcProduct.TYPE_GOODS, true));
    }

    @GetMapping("/product")
    public Result<List<SvcProduct>> products(@RequestParam(required = false) Long categoryId) {
        return Result.ok(productService.listOnSale(SvcProduct.TYPE_GOODS, categoryId));
    }

    @GetMapping("/product/{id}")
    public Result<SvcProduct> detail(@PathVariable Long id) {
        return Result.ok(productService.getOnSale(id, SvcProduct.TYPE_GOODS));
    }
}
