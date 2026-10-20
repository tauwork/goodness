package com.abos.server.module.partner.dto;

import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.partner.entity.PartnerCommission;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PartnerHomeVO {
    private boolean partner;
    private String name;
    private String code;
    private String sharePath;
    private BigDecimal commissionAmount;
    private long dealCount;
    private int customerCount;
    private List<Customer> customers;
    private List<PartnerCommission> deals;
}
