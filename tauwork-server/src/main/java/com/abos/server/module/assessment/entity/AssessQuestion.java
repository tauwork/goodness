package com.abos.server.module.assessment.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assess_question")
public class AssessQuestion extends BaseEntity {
    private Long quizId;
    private Integer seq;
    private String groupName;
    private String code;
    private String title;
    private Integer multi;
    private Integer weight;
    private Integer sortNo;
}
