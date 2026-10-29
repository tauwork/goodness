package com.abos.server.module.coreg.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("co_reg_member")
public class CoRegMember extends BaseEntity {

    public static final String SHAREHOLDER = "SHAREHOLDER";
    public static final String DIRECTOR = "DIRECTOR";
    public static final String BOTH = "BOTH";

    private Long caseId;
    private String name;
    private String role;
    private BigDecimal shareRatio;
    private String idFrontUrl;
    private String idBackUrl;
    private String idHoldUrl;
    private Integer sortNo;
}
