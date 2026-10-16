package com.abos.server.module.product.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品特征参数定义（分类级模板）：定义某分类的产品要填哪些参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_attr_def")
public class ProductAttrDef extends BaseEntity {

    private Long categoryId;
    /** 参数编码，如 capacity */
    private String attrCode;
    /** 参数名，如 容纳人数 */
    private String attrName;
    /** TEXT/NUMBER/SELECT/BOOLEAN */
    private String inputType;
    /** SELECT 选项 JSON [{value,label}] */
    private String options;
    private String unit;
    private Integer required;
    private Integer sortNo;
}
