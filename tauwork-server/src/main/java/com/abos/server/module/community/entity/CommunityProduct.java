package com.abos.server.module.community.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 社区×产品关联：同一空间产品可在不同社区店售卖，价格/库存/单位社区级覆盖
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_product")
public class CommunityProduct extends BaseEntity {

    private Long communityId;
    private Long productId;
    /** 社区级价格（空=用产品默认价） */
    private BigDecimal price;
    /** HOUR/DAY/MONTH/ONCE */
    private String priceUnit;
    /** 库存（NULL=不限） */
    private Integer stock;
    private Integer onSale;
    private Integer sortNo;
}
