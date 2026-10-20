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
    /** 1启用 0停用 */
    private Integer status;

    @TableField(exist = false)
    private String name;
    @TableField(exist = false)
    private String phone;
    @TableField(exist = false)
    private BigDecimal commissionAmount;
    @TableField(exist = false)
    private Long dealCount;
}
