package com.abos.server.module.finance.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("revenue_settlement")
public class RevenueSettlement extends BaseEntity {

    public static final String CONFIRMED = "CONFIRMED";
    public static final String VOID = "VOID";

    private Long customerId;
    private String title;
    private BigDecimal amount;
    private LocalDate settleDate;
    private String status;
    private String remark;

    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String customerPhone;
}
