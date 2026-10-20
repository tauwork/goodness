package com.abos.server.module.partner.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("partner")
public class Partner extends BaseEntity {

    private Long customerId;
    private String code;
    /** 上级合伙人 */
    private Long parentId;
    /** 1启用 0停用 */
    private Integer status;

    @TableField(exist = false)
    private String name;
    @TableField(exist = false)
    private String phone;
    @TableField(exist = false)
    private String parentName;
    @TableField(exist = false)
    private BigDecimal commissionAmount;
    @TableField(exist = false)
    private BigDecimal directAmount;
    @TableField(exist = false)
    private BigDecimal indirectAmount;
    @TableField(exist = false)
    private BigDecimal otherAmount;
    @TableField(exist = false)
    private BigDecimal withdrawnAmount;
    @TableField(exist = false)
    private BigDecimal balanceAmount;
    @TableField(exist = false)
    private Long dealCount;
    @TableField(exist = false)
    private Integer directCount;
    @TableField(exist = false)
    private Integer indirectCount;
}
