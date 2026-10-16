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
 * 小程序「服务栏目」（公开只读）：
 *  GET /api/mini/svc/category   服务分类（身份/公司注册/教育/续签…）
 *  GET /api/mini/svc/product    服务产品列表（可按分类）
 *  GET /api/mini/svc/product/{id} 服务详情
 */
@RestController
@RequestMapping("/api/mini/svc")
@RequiredArgsConstructor
public class SvcMiniController {

    private final SvcProductService productService;
    private final ProductCategoryService categoryService;

    @GetMapping("/category")
    public Result<List<ProductCategory>> categories() {
        return Result.ok(categoryService.listByType(SvcProduct.TYPE_SERVICE, true));
    }

    @GetMapping("/product")
    public Result<List<SvcProduct>> products(@RequestParam(required = false) Long categoryId) {
        return Result.ok(productService.listOnSale(SvcProduct.TYPE_SERVICE, categoryId));
    }

    @GetMapping("/product/{id}")
    public Result<SvcProduct> detail(@PathVariable Long id) {
        return Result.ok(productService.getById(id));
    }
}
