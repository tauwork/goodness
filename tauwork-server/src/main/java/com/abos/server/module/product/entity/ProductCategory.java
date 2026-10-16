package com.abos.server.module.product.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品分类（可配置）：服务类与空间类共用一张表，prod_type 区分
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_category")
public class ProductCategory extends BaseEntity {

    /** SERVICE / SPACE */
    private String prodType;
    private String code;
    private String name;
    private String icon;
    private Long parentId;
    private Integer sortNo;
    /** 1启用 0停用 */
    private Integer status;
}
