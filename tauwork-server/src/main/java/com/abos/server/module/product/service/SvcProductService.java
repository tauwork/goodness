package com.abos.server.module.product.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.mapper.SvcProductMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SvcProductService {

    private final SvcProductMapper productMapper;

    public IPage<SvcProduct> page(long page, long size, String bizLine, Integer onSale) {
        return productMapper.selectPage(new Page<>(page, size),
                Wrappers.<SvcProduct>lambdaQuery()
                        .eq(StringUtils.hasText(bizLine), SvcProduct::getBizLine, bizLine)
                        .eq(onSale != null, SvcProduct::getOnSale, onSale)
                        .orderByAsc(SvcProduct::getSortNo));
    }

    /** 按产品类型（SERVICE/SPACE）+ 分类查询在售产品 */
    public List<SvcProduct> listOnSale(String prodType, Long categoryId) {
        return productMapper.selectList(Wrappers.<SvcProduct>lambdaQuery()
                .eq(StringUtils.hasText(prodType), SvcProduct::getProdType, prodType)
                .eq(categoryId != null, SvcProduct::getCategoryId, categoryId)
                .eq(SvcProduct::getOnSale, 1)
                .orderByAsc(SvcProduct::getSortNo));
    }

    public SvcProduct getById(Long id) {
        SvcProduct p = productMapper.selectById(id);
        if (p == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return p;
    }

    public SvcProduct create(SvcProduct product) {
        product.setId(null);
        productMapper.insert(product);
        return product;
    }

    public SvcProduct update(SvcProduct product) {
        if (product.getId() == null || productMapper.selectById(product.getId()) == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        productMapper.updateById(product);
        return getById(product.getId());
    }

    public void offSale(Long id) {
        SvcProduct p = getById(id);
        p.setOnSale(0);
        productMapper.updateById(p);
    }
}
