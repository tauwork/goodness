package com.abos.server.module.coreg.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("co_reg_case")
public class CoRegCase extends BaseEntity {

    public static final String NAME = "NAME";
    public static final String PROFILE = "PROFILE";
    public static final String KYC = "KYC";
    public static final String SIGN = "SIGN";
    public static final String FILING = "FILING";
    public static final String CERT = "CERT";
    public static final String DONE = "DONE";

    public static final String WAIT_USER = "WAIT_USER";
    public static final String WAIT_REVIEW = "WAIT_REVIEW";
    public static final String REJECTED = "REJECTED";
    public static final String WAIT_OPS = "WAIT_OPS";
    public static final String STEP_DONE = "DONE";

    public static final String ACTIVE = "ACTIVE";
    public static final String CASE_DONE = "DONE";
    public static final String CANCELED = "CANCELED";

    private String orderNo;
    private Long svcOrderId;
    private Long customerId;
    private Long productId;
    private String productName;
    private String step;
    private String stepStatus;
    private String caseStatus;
    private String companyName;
    private String companyNameEn;
    private String bizScope;
    private BigDecimal capitalAmount;
    private String capitalCurrency;
    private String remark;
}
