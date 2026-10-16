package com.abos.server.module.assessment.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assess_quiz")
public class AssessQuiz extends BaseEntity {
    private String version;
    private Integer status;
}
