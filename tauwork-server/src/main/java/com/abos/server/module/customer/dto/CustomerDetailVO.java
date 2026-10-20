package com.abos.server.module.customer.dto;

import com.abos.server.module.booking.entity.SvcBooking;
import com.abos.server.module.crm.entity.CrmFollow;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.entity.FamilyMember;
import com.abos.server.module.lead.entity.Lead;
import lombok.Data;

import java.util.List;

@Data
public class CustomerDetailVO {
    private Customer customer;
    private List<FamilyMember> family;
    private List<CrmFollow> follows;
    private List<SvcBooking> appointments;
    private List<Lead> leads;
}
