package com.abos.server.module.community.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.community.dto.CommunityCardVO;
import com.abos.server.module.community.dto.CommunityProductVO;
import com.abos.server.module.community.entity.Community;
import com.abos.server.module.community.entity.CommunityProduct;
import com.abos.server.module.community.mapper.CommunityMapper;
import com.abos.server.security.AccessScope;
import com.abos.server.module.community.mapper.CommunityProductMapper;
import com.abos.server.module.product.entity.ProductCategory;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.mapper.ProductCategoryMapper;
import com.abos.server.module.product.mapper.SvcProductMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
    private final AccessScope access;
    private final SvcProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;

    /* ---------------- 后台：社区店 ---------------- */

    public List<Community> adminList(String city, String status) {
        AccessScope.View scope = access.current();
        if (!scope.all() && scope.communityIds().isEmpty()) {
            return List.of();
        }
        return communityMapper.selectList(Wrappers.<Community>lambdaQuery()
                .eq(StringUtils.hasText(city), Community::getCity, city)
                .eq(StringUtils.hasText(status), Community::getStatus, status)
                .in(!scope.all(), Community::getId, scope.communityIds())
                .orderByAsc(Community::getSortNo));
    }

    public Community getById(Long id) {
        Community c = communityMapper.selectById(id);
        if (c == null) throw new BizException(ErrorCode.NOT_FOUND, "社区不存在");
        return c;
    }

    public Community create(Community community) {
        checkBasic(community);
        if (!List.of("OPEN", "CLOSED").contains(community.getStatus())) {
            community.setStatus("OPEN");
        }
        community.setId(null);
        if (community.getSortNo() == null) community.setSortNo(0);
        communityMapper.insert(community);
        return community;
    }

    public Community update(Community community) {
        getById(community.getId());
        checkBasic(community);
        if (!List.of("OPEN", "CLOSED", "PREPARING").contains(community.getStatus())) {
            community.setStatus("OPEN");
        }
        communityMapper.updateById(community);
        return communityMapper.selectById(community.getId());
    }

    /** 上架后小程序可见；下架后从社区列表消失。 */
    public Community setOnSale(Long id, boolean on) {
        Community community = getById(id);
        checkBasic(community);
        community.setStatus(on ? "OPEN" : "CLOSED");
        communityMapper.updateById(community);
        return community;
    }

    /* ---------------- 后台：社区×产品关联 ---------------- */

    public CommunityProduct bindProduct(CommunityProduct cp) {
        getById(cp.getCommunityId());
        SvcProduct p = productMapper.selectById(cp.getProductId());
        if (p == null || !SvcProduct.TYPE_SPACE.equals(p.getProdType())) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND, "仅空间产品可关联到社区");
        }
        Long n = communityProductMapper.selectCount(Wrappers.<CommunityProduct>lambdaQuery()
                .eq(CommunityProduct::getCommunityId, cp.getCommunityId())
                .eq(CommunityProduct::getProductId, cp.getProductId()));
        if (n != null && n > 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该产品已关联此社区");
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

    /** 营业中的社区（可按城市筛选） */
    public List<Community> miniList(String city) {
        return communityMapper.selectList(Wrappers.<Community>lambdaQuery()
                .eq(Community::getStatus, "OPEN")
                .eq(StringUtils.hasText(city), Community::getCity, city)
                .orderByAsc(Community::getSortNo));
    }

    /** 小程序社区卡片：距离、工位/会议室/房间数量与起步价。 */
    public List<CommunityCardVO> miniCards(Double lat, Double lng, String keyword) {
        List<Community> communities = communityMapper.selectList(Wrappers.<Community>lambdaQuery()
                .eq(Community::getStatus, "OPEN")
                .and(StringUtils.hasText(keyword), w -> w.like(Community::getName, keyword)
                        .or().like(Community::getAddress, keyword)
                        .or().like(Community::getDistrict, keyword))
                .orderByAsc(Community::getSortNo));
        if (communities.isEmpty()) {
            return List.of();
        }
        List<Long> ids = communities.stream().map(Community::getId).toList();
        List<CommunityProduct> bindings = communityProductMapper.selectList(Wrappers.<CommunityProduct>lambdaQuery()
                .in(CommunityProduct::getCommunityId, ids)
                .eq(CommunityProduct::getOnSale, 1));
        Map<Long, SvcProduct> productMap = Map.of();
        Map<Long, ProductCategory> catMap = Map.of();
        if (!bindings.isEmpty()) {
            List<Long> productIds = bindings.stream().map(CommunityProduct::getProductId).distinct().toList();
            productMap = productMapper.selectBatchIds(productIds).stream()
                    .collect(Collectors.toMap(SvcProduct::getId, Function.identity()));
            catMap = categoryMapper.selectList(Wrappers.<ProductCategory>lambdaQuery()
                            .eq(ProductCategory::getProdType, SvcProduct.TYPE_SPACE))
                    .stream().collect(Collectors.toMap(ProductCategory::getId, Function.identity()));
        }
        Map<Long, List<CommunityProduct>> byCommunity = bindings.stream()
                .collect(Collectors.groupingBy(CommunityProduct::getCommunityId));

        List<CommunityCardVO> cards = new ArrayList<>();
        for (Community community : communities) {
            CommunityCardVO card = new CommunityCardVO();
            card.setId(community.getId());
            card.setName(community.getName());
            card.setCity(community.getCity());
            card.setDistrict(community.getDistrict());
            card.setAddress(community.getAddress());
            card.setCoverUrl(community.getCoverUrl());
            card.setOpenHours(community.getOpenHours());
            card.setLat(community.getLat());
            card.setLng(community.getLng());
            if (lat != null && lng != null && community.getLat() != null && community.getLng() != null) {
                card.setDistanceKm(roundKm(distanceKm(lat, lng, community.getLat().doubleValue(), community.getLng().doubleValue())));
            }
            Map<String, Integer> counts = new HashMap<>();
            Map<String, BigDecimal> prices = new HashMap<>();
            BigDecimal overall = null;
            for (CommunityProduct binding : byCommunity.getOrDefault(community.getId(), List.of())) {
                SvcProduct product = productMap.get(binding.getProductId());
                if (product == null) continue;
                ProductCategory category = product.getCategoryId() == null ? null : catMap.get(product.getCategoryId());
                String code = category == null ? "" : category.getCode();
                int qty = binding.getStock() != null && binding.getStock() > 0 ? binding.getStock() : 1;
                counts.merge(code, qty, Integer::sum);
                BigDecimal price = binding.getPrice() != null ? binding.getPrice() : product.getPriceValue();
                if (price != null) {
                    prices.merge(code, price, (a, b) -> a.compareTo(b) <= 0 ? a : b);
                    overall = overall == null || price.compareTo(overall) < 0 ? price : overall;
                }
            }
            card.setDeskCount(counts.getOrDefault("DESK", 0));
            card.setMeetingCount(counts.getOrDefault("MEETING", 0));
            card.setRoomCount(counts.getOrDefault("ROOM", 0));
            card.setDeskPriceFrom(prices.get("DESK"));
            card.setMeetingPriceFrom(prices.get("MEETING"));
            card.setRoomPriceFrom(prices.get("ROOM"));
            card.setPriceFrom(overall);
            cards.add(card);
        }
        if (lat != null && lng != null) {
            cards.sort(Comparator.comparing(CommunityCardVO::getDistanceKm, Comparator.nullsLast(Double::compareTo)));
        }
        return cards;
    }

    /** 小程序只展示已上架社区。 */
    public Community miniGet(Long id) {
        Community community = getById(id);
        if (!"OPEN".equals(community.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "社区未上架");
        }
        return community;
    }

    /** 某社区店内可售空间产品（聚合产品信息、分类、社区级价格库存） */
    public List<CommunityProductVO> miniCommunityProducts(Long communityId) {
        miniGet(communityId);
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

    private void checkBasic(Community community) {
        if (!StringUtils.hasText(community.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写社区名称");
        }
        if (!StringUtils.hasText(community.getCity())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写城市");
        }
        if (!StringUtils.hasText(community.getAddress())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写地址");
        }
    }

    private static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double radius = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return radius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private static double roundKm(double km) {
        return Math.round(km * 100.0) / 100.0;
    }
}
