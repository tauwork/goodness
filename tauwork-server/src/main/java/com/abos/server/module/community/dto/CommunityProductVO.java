package com.abos.server.module.community.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 小程序「空间栏目」展示 VO：社区店内可售的空间产品（产品信息 + 社区级价格库存）
 */
@Data
public class CommunityProductVO {

    private Long communityProductId;
    private Long productId;
    private String name;
    private String subtitle;
    /** 分类名（工位/独立房间/会议室/公司地址/其他） */
    private String categoryName;
    /** 分类编码，前端映射图标 */
    private String categoryCode;
    /** 特征参数 JSON */
    private String attrs;
    /** 生效价格（社区价优先，否则产品默认价） */
    private BigDecimal price;
    /** HOUR/DAY/MONTH/ONCE/null按报价 */
    private String priceUnit;
    /** 报价方式 QUOTE/FIXED/STAGED/MEMBER */
    private String priceMode;
    /** 库存（null=不限） */
    private Integer stock;
    private Integer sortNo;
}
