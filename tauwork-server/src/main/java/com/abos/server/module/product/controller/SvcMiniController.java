package com.abos.server.module.product.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.product.dto.ConsultCatalogVO;
import com.abos.server.module.product.dto.ConsultSubmitDTO;
import com.abos.server.module.product.entity.ProductCategory;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.service.ConsultCatalogService;
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
    private final ConsultCatalogService consultCatalogService;

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

    /** 1v1 咨询：服务大类与对接顾问 */
    @GetMapping("/consult")
    public Result<ConsultCatalogVO> consult() {
        return Result.ok(consultCatalogService.catalog());
    }

    /** 1v1 咨询留资，无需登录 */
    @PostMapping("/consult")
    public Result<Void> consult(@RequestBody ConsultSubmitDTO dto) {
        consultCatalogService.submit(dto);
        return Result.ok();
    }
}
