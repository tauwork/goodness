package com.abos.server.module.workorder.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_order")
public class WorkOrder extends BaseEntity {

    public static final String OPEN = "OPEN";
    public static final String PROCESSING = "PROCESSING";
    public static final String DONE = "DONE";
    public static final String CANCELED = "CANCELED";

    private String title;
    private Long customerId;
    private Long assigneeId;
    private String status;
    private String content;
    private String result;

    @TableField(exist = false)
    private String customerName;
    @TableField(exist = false)
    private String customerPhone;
    @TableField(exist = false)
    private String assigneeName;
    @TableField(exist = false)
    private List<WorkOrderLog> logs;
}
