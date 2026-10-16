package com.abos.server.module.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.product.entity.ProductAttrDef;
import com.abos.server.module.product.entity.ProductCategory;
import com.abos.server.module.product.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台：产品分类配置 + 分类特征参数定义
 */
@RestController
@RequestMapping("/api/admin/product-category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @GetMapping("/list")
    public Result<List<ProductCategory>> list(@RequestParam(required = false) String prodType,
                                              @RequestParam(defaultValue = "true") boolean onlyEnabled) {
        return Result.ok(categoryService.listByType(prodType, onlyEnabled));
    }

    @PostMapping
    @SaCheckPermission("product:manage")
    public Result<ProductCategory> create(@RequestBody ProductCategory category) {
        return Result.ok(categoryService.create(category));
    }

    @PutMapping
    @SaCheckPermission("product:manage")
    public Result<ProductCategory> update(@RequestBody ProductCategory category) {
        return Result.ok(categoryService.update(category));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("product:manage")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.ok();
    }

    @PutMapping("/{id}/disable")
    @SaCheckPermission("product:manage")
    public Result<Void> disable(@PathVariable Long id) {
        categoryService.disable(id);
        return Result.ok();
    }

    /* ---------- 特征参数定义 ---------- */

    @GetMapping("/{categoryId}/attr-def")
    public Result<List<ProductAttrDef>> listAttrDefs(@PathVariable Long categoryId) {
        return Result.ok(categoryService.listAttrDefs(categoryId));
    }

    @PostMapping("/attr-def")
    @SaCheckPermission("product:manage")
    public Result<ProductAttrDef> createAttrDef(@RequestBody ProductAttrDef def) {
        return Result.ok(categoryService.createAttrDef(def));
    }

    @DeleteMapping("/attr-def/{id}")
    @SaCheckPermission("product:manage")
    public Result<Void> deleteAttrDef(@PathVariable Long id) {
        categoryService.deleteAttrDef(id);
        return Result.ok();
    }
}
