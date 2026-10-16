package com.abos.server.module.assessment.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assess_option")
public class AssessOption extends BaseEntity {
    private Long questionId;
    private Integer seq;
    private String label;
    private Integer score;
    private String tag;
}
