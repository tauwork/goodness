package com.abos.server.module.finance.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("deposit_record")
public class DepositRecord extends BaseEntity {

    public static final String DEPOSIT = "DEPOSIT";
    public static final String PLEDGE = "PLEDGE";
    public static final String HELD = "HELD";
    public static final String REFUNDED = "REFUNDED";
    public static final String APPLIED = "APPLIED";

    private Long customerId;
    private String kind;
    private BigDecimal amount;
    private String status;
    private String remark;

    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String customerPhone;
}
