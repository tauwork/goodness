package com.abos.server.module.product.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.booking.mapper.SvcBookingMapper;
import com.abos.server.module.booking.entity.SvcBooking;
import com.abos.server.module.community.entity.CommunityProduct;
import com.abos.server.module.community.mapper.CommunityProductMapper;
import com.abos.server.module.order.entity.Order;
import com.abos.server.module.order.mapper.OrderMapper;
import com.abos.server.module.product.entity.ProductCategory;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.mapper.ProductCategoryMapper;
import com.abos.server.module.product.mapper.SvcProductMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SvcProductService {

    private final SvcProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;
    private final CommunityProductMapper communityProductMapper;
    private final OrderMapper orderMapper;
    private final SvcBookingMapper svcBookingMapper;

    public IPage<SvcProduct> page(long page, long size, String prodType, Long categoryId,
                                  Integer onSale, String keyword) {
        return productMapper.selectPage(new Page<>(page, size),
                Wrappers.<SvcProduct>lambdaQuery()
                        .eq(StringUtils.hasText(prodType), SvcProduct::getProdType, prodType)
                        .eq(categoryId != null, SvcProduct::getCategoryId, categoryId)
                        .eq(onSale != null, SvcProduct::getOnSale, onSale)
                        .like(StringUtils.hasText(keyword), SvcProduct::getName, keyword)
                        .orderByAsc(SvcProduct::getSortNo)
                        .orderByDesc(SvcProduct::getId));
    }

    /** 按产品类型 + 分类查询在售产品 */
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

    public SvcProduct getOnSale(Long id, String prodType) {
        SvcProduct p = getById(id);
        if (!prodType.equals(p.getProdType()) || p.getOnSale() == null || p.getOnSale() != 1) {
            throw new BizException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }
        return p;
    }

    public SvcProduct create(SvcProduct product) {
        product.setId(null);
        prepare(product);
        productMapper.insert(product);
        return product;
    }

    public SvcProduct update(SvcProduct product) {
        if (product.getId() == null || productMapper.selectById(product.getId()) == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        prepare(product);
        productMapper.updateById(product);
        return getById(product.getId());
    }

    public void setOnSale(Long id, int onSale) {
        SvcProduct p = getById(id);
        p.setOnSale(onSale);
        prepare(p);
        productMapper.updateById(p);
    }

    public void offSale(Long id) {
        setOnSale(id, 0);
    }

    /** 无社区店关联、预约、订单时才允许逻辑删除 */
    public void delete(Long id) {
        getById(id);
        Long binds = communityProductMapper.selectCount(Wrappers.<CommunityProduct>lambdaQuery()
                .eq(CommunityProduct::getProductId, id));
        if (binds != null && binds > 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "已上架到社区，请先解除关联");
        }
        Long bookings = svcBookingMapper.selectCount(Wrappers.<SvcBooking>lambdaQuery()
                .eq(SvcBooking::getProductId, id));
        if (bookings != null && bookings > 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "已有服务预约，不能删除");
        }
        Long orders = orderMapper.selectCount(Wrappers.<Order>lambdaQuery().eq(Order::getProductId, id));
        if (orders != null && orders > 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "已有订单，不能删除");
        }
        productMapper.deleteById(id);
    }

    private void prepare(SvcProduct product) {
        if (!List.of(SvcProduct.TYPE_GOODS, SvcProduct.TYPE_SERVICE, SvcProduct.TYPE_SPACE)
                .contains(product.getProdType())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "类型必须是 GOODS/SERVICE/SPACE");
        }
        if (!StringUtils.hasText(product.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "名称不能为空");
        }
        if (product.getCategoryId() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择分类");
        }
        ProductCategory category = categoryMapper.selectById(product.getCategoryId());
        if (category == null || !product.getProdType().equals(category.getProdType())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "分类与商品类型不一致");
        }
        if (product.getOnSale() == null) {
            product.setOnSale(1);
        }
        if (product.getSortNo() == null) {
            product.setSortNo(0);
        }
        if (SvcProduct.TYPE_GOODS.equals(product.getProdType())) {
            product.setPriceMode("FIXED");
            if (product.getPriceValue() == null || product.getPriceValue().compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(ErrorCode.BAD_REQUEST, "请填写售价");
            }
            if (product.getStock() == null || product.getStock() < 0) {
                throw new BizException(ErrorCode.BAD_REQUEST, "请填写库存");
            }
            if (product.getOnSale() == 1 && !StringUtils.hasText(product.getCoverUrl())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "上架的通用商品需要主图");
            }
        }
    }
}
