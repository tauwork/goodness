package com.abos.server.module.contract.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("contract_order")
public class ContractOrder extends BaseEntity {

    private Long customerId;
    private BigDecimal amount;
    private String fileUrl;
    private String fileName;
    private String remark;

    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String customerPhone;
}
