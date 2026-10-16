package com.abos.server.module.product.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("svc_product")
public class SvcProduct extends BaseEntity {

    /** SERVICE 服务产品 / SPACE 空间产品 */
    public static final String TYPE_SERVICE = "SERVICE";
    public static final String TYPE_SPACE = "SPACE";

    private String prodType;
    /** 分类 product_category.id */
    private Long categoryId;
    /** IDENTITY/EDU/STARTUP（服务分类编码，兼容旧数据；新数据用 categoryId） */
    private String bizLine;
    private String name;
    private String subtitle;
    /** QUOTE/FIXED/STAGED/MEMBER */
    private String priceMode;
    private BigDecimal priceValue;
    private BigDecimal commissionL1;
    private BigDecimal commissionL2;
    private Long flowTplId;
    /** 材料清单模板 JSON */
    private String materialTpl;
    /** 特征参数值 {attr_code: value} */
    private String attrs;
    private Integer onSale;
    private Integer sortNo;
}
