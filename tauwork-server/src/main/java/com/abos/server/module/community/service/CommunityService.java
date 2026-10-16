package com.abos.server.module.community.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.community.dto.CommunityProductVO;
import com.abos.server.module.community.entity.Community;
import com.abos.server.module.community.entity.CommunityProduct;
import com.abos.server.module.community.mapper.CommunityMapper;
import com.abos.server.module.community.mapper.CommunityProductMapper;
import com.abos.server.module.product.entity.ProductCategory;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.mapper.ProductCategoryMapper;
import com.abos.server.module.product.mapper.SvcProductMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 社区管理：社区店 CRUD + 社区×产品关联 + 小程序空间栏目聚合
 */
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityMapper communityMapper;
    private final CommunityProductMapper communityProductMapper;
    private final SvcProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;

    /* ---------------- 后台：社区店 ---------------- */

    public List<Community> adminList(String city, String status) {
        return communityMapper.selectList(Wrappers.<Community>lambdaQuery()
                .eq(StringUtils.hasText(city), Community::getCity, city)
                .eq(StringUtils.hasText(status), Community::getStatus, status)
                .orderByAsc(Community::getSortNo));
    }

    public Community getById(Long id) {
        Community c = communityMapper.selectById(id);
        if (c == null) throw new BizException(ErrorCode.NOT_FOUND, "社区店不存在");
        return c;
    }

    public Community create(Community community) {
        if (!List.of("OPEN", "PREPARING", "CLOSED").contains(community.getStatus())) {
            community.setStatus("OPEN");
        }
        community.setId(null);
        if (community.getSortNo() == null) community.setSortNo(0);
        communityMapper.insert(community);
        return community;
    }

    public Community update(Community community) {
        getById(community.getId());
        communityMapper.updateById(community);
        return communityMapper.selectById(community.getId());
    }

    /* ---------------- 后台：社区×产品关联 ---------------- */

    public CommunityProduct bindProduct(CommunityProduct cp) {
        getById(cp.getCommunityId());
        SvcProduct p = productMapper.selectById(cp.getProductId());
        if (p == null || !SvcProduct.TYPE_SPACE.equals(p.getProdType())) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND, "仅空间类产品可关联社区店");
        }
        Long n = communityProductMapper.selectCount(Wrappers.<CommunityProduct>lambdaQuery()
                .eq(CommunityProduct::getCommunityId, cp.getCommunityId())
                .eq(CommunityProduct::getProductId, cp.getProductId()));
        if (n != null && n > 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该产品已关联此社区店");
        }
        cp.setId(null);
        if (cp.getOnSale() == null) cp.setOnSale(1);
        if (cp.getSortNo() == null) cp.setSortNo(0);
        communityProductMapper.insert(cp);
        return cp;
    }

    public void unbindProduct(Long id) {
        if (communityProductMapper.selectById(id) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "关联不存在");
        }
        communityProductMapper.deleteById(id);
    }

    public List<CommunityProduct> listBindings(Long communityId) {
        return communityProductMapper.selectList(Wrappers.<CommunityProduct>lambdaQuery()
                .eq(CommunityProduct::getCommunityId, communityId)
                .orderByAsc(CommunityProduct::getSortNo));
    }

    /* ---------------- 小程序：空间栏目 ---------------- */

    /** 营业中的社区店列表（可按城市筛选） */
    public List<Community> miniList(String city) {
        return communityMapper.selectList(Wrappers.<Community>lambdaQuery()
                .eq(Community::getStatus, "OPEN")
                .eq(StringUtils.hasText(city), Community::getCity, city)
                .orderByAsc(Community::getSortNo));
    }

    /** 某社区店内可售空间产品（聚合产品信息、分类、社区级价格库存） */
    public List<CommunityProductVO> miniCommunityProducts(Long communityId) {
        getById(communityId);
        List<CommunityProduct> bindings = communityProductMapper.selectList(
                Wrappers.<CommunityProduct>lambdaQuery()
                        .eq(CommunityProduct::getCommunityId, communityId)
                        .eq(CommunityProduct::getOnSale, 1)
                        .orderByAsc(CommunityProduct::getSortNo));
        if (bindings.isEmpty()) {
            return List.of();
        }
        List<Long> productIds = bindings.stream().map(CommunityProduct::getProductId).toList();
        Map<Long, SvcProduct> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(SvcProduct::getId, Function.identity()));
        Map<Long, ProductCategory> catMap = categoryMapper.selectList(
                        Wrappers.<ProductCategory>lambdaQuery().eq(ProductCategory::getProdType, "SPACE"))
                .stream().collect(Collectors.toMap(ProductCategory::getId, Function.identity()));

        return bindings.stream().map(b -> {
            SvcProduct p = productMap.get(b.getProductId());
            if (p == null) return null;
            CommunityProductVO vo = new CommunityProductVO();
            vo.setCommunityProductId(b.getId());
            vo.setProductId(p.getId());
            vo.setName(p.getName());
            vo.setSubtitle(p.getSubtitle());
            vo.setAttrs(p.getAttrs());
            vo.setPriceMode(p.getPriceMode());
            vo.setPrice(b.getPrice() != null ? b.getPrice() : p.getPriceValue());
            vo.setPriceUnit(b.getPriceUnit());
            vo.setStock(b.getStock());
            vo.setSortNo(b.getSortNo());
            ProductCategory cat = p.getCategoryId() != null ? catMap.get(p.getCategoryId()) : null;
            if (cat != null) {
                vo.setCategoryName(cat.getName());
                vo.setCategoryCode(cat.getCode());
            }
            return vo;
        }).filter(java.util.Objects::nonNull).toList();
    }
}
