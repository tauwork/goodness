package com.abos.server.module.partner.dto;

import com.abos.server.module.partner.entity.Partner;
import com.abos.server.module.partner.entity.PartnerCommission;
import com.abos.server.module.partner.entity.PartnerWithdraw;
import lombok.Data;

import java.util.List;

@Data
public class PartnerDetailVO {
    private Partner partner;
    private List<PartnerCustomerVO> customers;
    private List<PartnerCommission> deals;
    private List<PartnerWithdraw> withdraws;
}
