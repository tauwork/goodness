package com.abos.server.module.community.entity;

import com.abos.server.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 社区店：一个具体空间（参考 WeWork Location / DoBe Hub 社区）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community")
public class Community extends BaseEntity {

    private String name;
    private String city;
    private String district;
    private String address;
    private BigDecimal lat;
    private BigDecimal lng;
    private String openHours;
    private String coverUrl;
    /** 设施标签 JSON ["投屏","Wi-Fi"] */
    private String facilities;
    private String intro;
    /** OPEN/PREPARING/CLOSED */
    private String status;
    private Integer sortNo;
}
