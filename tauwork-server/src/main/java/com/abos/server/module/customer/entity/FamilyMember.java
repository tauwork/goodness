package com.abos.server.module.customer.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("family_member")
public class FamilyMember extends BaseEntity {

    private Long customerId;
    /** SELF/SPOUSE/CHILD */
    private String relation;
    private String name;
    private LocalDate birthDate;
    private String schoolStage;
}
