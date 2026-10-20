package com.abos.server.module.partner.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("partner_rule")
public class PartnerRule extends BaseEntity {

    private Long categoryId;
    private BigDecimal rate;

    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private String prodType;
}
