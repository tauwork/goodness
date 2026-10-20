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
@TableName("receivable_bill")
public class ReceivableBill extends BaseEntity {

    public static final String OPEN = "OPEN";
    public static final String PARTIAL = "PARTIAL";
    public static final String SETTLED = "SETTLED";
    public static final String VOID = "VOID";

    private Long customerId;
    private String title;
    private BigDecimal amount;
    private BigDecimal received;
    private String status;
    private LocalDate dueDate;
    private String remark;

    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String customerPhone;
}
