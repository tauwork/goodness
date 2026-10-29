package com.abos.server.module.product.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.product.entity.ProductAttrDef;
import com.abos.server.module.product.entity.ProductCategory;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.mapper.ProductAttrDefMapper;
import com.abos.server.module.product.mapper.ProductCategoryMapper;
import com.abos.server.module.product.mapper.SvcProductMapper;
import com.abos.server.module.team.mapper.ConsultantMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 产品分类与特征参数配置：
 * 分类决定"卖什么类型的产品"，参数定义决定"该类产品要填哪些特征"（如会议室容量/地址类型）
 */
@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private static final List<String> PROD_TYPES = List.of(
            SvcProduct.TYPE_SERVICE, SvcProduct.TYPE_SPACE, SvcProduct.TYPE_GOODS);

    private final ProductCategoryMapper categoryMapper;
    private final ProductAttrDefMapper attrDefMapper;
    private final SvcProductMapper productMapper;
    private final ConsultantMapper consultantMapper;

    /* ---------------- 分类 ---------------- */

    public List<ProductCategory> listByType(String prodType, boolean onlyEnabled) {
        return categoryMapper.selectList(Wrappers.<ProductCategory>lambdaQuery()
                .eq(StringUtils.hasText(prodType), ProductCategory::getProdType, prodType)
                .eq(onlyEnabled, ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSortNo));
    }

    public ProductCategory create(ProductCategory category) {
        if (!PROD_TYPES.contains(category.getProdType())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "prodType 必须是 GOODS/SERVICE/SPACE");
        }
        if (!StringUtils.hasText(category.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "分类名称不能为空");
        }
        if (!StringUtils.hasText(category.getCode())) {
            category.setCode(category.getProdType().substring(0, 1) + System.currentTimeMillis());
        }
        if (StringUtils.hasText(category.getCode())) {
            Long n = categoryMapper.selectCount(Wrappers.<ProductCategory>lambdaQuery()
                    .eq(ProductCategory::getProdType, category.getProdType())
                    .eq(ProductCategory::getCode, category.getCode()));
            if (n != null && n > 0) {
                throw new BizException(ErrorCode.BAD_REQUEST, "分类编码已存在: " + category.getCode());
            }
        }
        category.setId(null);
        if (category.getStatus() == null) category.setStatus(1);
        if (category.getSortNo() == null) category.setSortNo(0);
        ensureConsultant(category.getConsultantId());
        categoryMapper.insert(category);
        return category;
    }

    public ProductCategory update(ProductCategory category) {
        if (category.getId() == null || categoryMapper.selectById(category.getId()) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "分类不存在");
        }
        ensureConsultant(category.getConsultantId());
        categoryMapper.updateById(category);
        return categoryMapper.selectById(category.getId());
    }

    private void ensureConsultant(Long consultantId) {
        if (consultantId != null && consultantMapper.selectById(consultantId) == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "顾问不存在");
        }
    }

    public void disable(Long id) {
        ProductCategory c = categoryMapper.selectById(id);
        if (c == null) throw new BizException(ErrorCode.NOT_FOUND, "分类不存在");
        c.setStatus(0);
        categoryMapper.updateById(c);
    }

    /* ---------------- 特征参数定义 ---------------- */

    public List<ProductAttrDef> listAttrDefs(Long categoryId) {
        return attrDefMapper.selectList(Wrappers.<ProductAttrDef>lambdaQuery()
                .eq(ProductAttrDef::getCategoryId, categoryId)
                .orderByAsc(ProductAttrDef::getSortNo));
    }

    public ProductAttrDef createAttrDef(ProductAttrDef def) {
        if (!List.of("TEXT", "NUMBER", "SELECT", "BOOLEAN").contains(def.getInputType())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "inputType 必须是 TEXT/NUMBER/SELECT/BOOLEAN");
        }
        def.setId(null);
        if (def.getSortNo() == null) def.setSortNo(0);
        attrDefMapper.insert(def);
        return def;
    }

    public void deleteAttrDef(Long id) {
        if (attrDefMapper.selectById(id) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "参数定义不存在");
        }
        attrDefMapper.deleteById(id);
    }

    /** 删除分类前校验：有参数定义或有产品挂载则拒绝 */
    @Transactional
    public void deleteCategory(Long id) {
        Long attrCount = attrDefMapper.selectCount(Wrappers.<ProductAttrDef>lambdaQuery()
                .eq(ProductAttrDef::getCategoryId, id));
        if (attrCount != null && attrCount > 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该分类下有特征参数定义，请先删除参数");
        }
        Long productCount = productMapper.selectCount(Wrappers.<SvcProduct>lambdaQuery()
                .eq(SvcProduct::getCategoryId, id));
        if (productCount != null && productCount > 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该分类下还有商品，不能删除");
        }
        categoryMapper.deleteById(id);
    }
}
