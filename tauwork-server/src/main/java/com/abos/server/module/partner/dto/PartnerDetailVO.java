package com.abos.server.module.partner.dto;

import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.partner.entity.Partner;
import com.abos.server.module.partner.entity.PartnerCommission;
import lombok.Data;

import java.util.List;

@Data
public class PartnerDetailVO {
    private Partner partner;
    private List<Customer> customers;
    private List<PartnerCommission> deals;
}
