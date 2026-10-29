package com.abos.server.module.finance.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对公收款账户。全库只保留一条，供小程序对公转账展示。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("finance_account")
public class FinanceAccount extends BaseEntity {

    public static final long SINGLE_ID = 1L;

    private String accountName;
    private String bankName;
    private String accountNo;
    private String branch;
}
