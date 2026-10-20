package com.abos.server.module.community.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 小程序社区列表卡片：封面、距离、工位/会议室数量、起步价。
 */
@Data
public class CommunityCardVO {

    private Long id;
    private String name;
    private String city;
    private String district;
    private String address;
    private String coverUrl;
    private String openHours;
    private BigDecimal lat;
    private BigDecimal lng;
    /** 与传入坐标的距离，公里；没有坐标时为空 */
    private Double distanceKm;
    private Integer deskCount;
    private Integer meetingCount;
    private Integer roomCount;
    private BigDecimal priceFrom;
    private BigDecimal deskPriceFrom;
    private BigDecimal meetingPriceFrom;
    private BigDecimal roomPriceFrom;
}
