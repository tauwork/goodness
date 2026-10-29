package com.abos.server.module.coreg.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("co_reg_log")
public class CoRegLog extends BaseEntity {

    private Long caseId;
    private String step;
    private String action;
    private String remark;
}
