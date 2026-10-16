package com.abos.server.module.assessment.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("assess_recommend")
public class AssessRecommend extends BaseEntity {
    private Long quizId;
    private String code;
    private String name;
    private Long productId;
    private Integer minScore;
    private String requireTag;
    private Integer priority;
}
