package com.abos.server.module.workorder.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_order_log")
public class WorkOrderLog extends BaseEntity {

    public static final String CREATE = "CREATE";
    public static final String START = "START";
    public static final String FINISH = "FINISH";
    public static final String CANCEL = "CANCEL";
    public static final String NOTE = "NOTE";

    private Long workOrderId;
    private String action;
    private String content;

    @TableField(exist = false)
    private String operatorName;
}
