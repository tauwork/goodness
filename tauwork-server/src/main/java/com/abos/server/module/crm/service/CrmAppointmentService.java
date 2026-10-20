package com.abos.server.module.crm.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.crm.dto.AppointmentCreateDTO;
import com.abos.server.module.crm.entity.CrmAppointment;
import com.abos.server.module.crm.mapper.CrmAppointmentMapper;
import com.abos.server.module.customer.dto.CustomerProfileDTO;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.customer.service.CustomerAdminService;
import com.abos.server.module.sysuser.service.SysUserService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CrmAppointmentService {

    private static final Set<String> TYPES = Set.of("VISIT", "STORE", "VIDEO", "PHONE", "MEETING");
    private static final Set<String> STATUSES = Set.of(
            CrmAppointment.PENDING, CrmAppointment.CONFIRMED, CrmAppointment.DONE,
            CrmAppointment.CANCELED, CrmAppointment.NO_SHOW);

    private final CrmAppointmentMapper appointmentMapper;
    private final CustomerMapper customerMapper;
    private final CustomerAdminService customerAdmin;
    private final SysUserService staff;

    public List<CrmAppointment> list(LocalDate bookDate, String status, String keyword) {
        List<CrmAppointment> rows = appointmentMapper.selectList(Wrappers.<CrmAppointment>lambdaQuery()
                .eq(bookDate != null, CrmAppointment::getBookDate, bookDate)
                .eq(StringUtils.hasText(status), CrmAppointment::getStatus, status)
                .like(StringUtils.hasText(keyword), CrmAppointment::getTopic, keyword)
                .orderByDesc(CrmAppointment::getBookDate)
                .orderByAsc(CrmAppointment::getTimeSlot));
        fill(rows);
        if (!StringUtils.hasText(keyword)) {
            return rows;
        }
        String key = keyword.trim();
        return rows.stream().filter(row ->
                contains(row.getTopic(), key)
                        || contains(row.getCustomerName(), key)
                        || contains(row.getCustomerPhone(), key)
                        || contains(row.getPlace(), key)).toList();
    }

    @Transactional
    public CrmAppointment create(AppointmentCreateDTO dto, Long operatorId) {
        if (!TYPES.contains(dto.getVisitType())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择预约方式");
        }
        if (!StringUtils.hasText(dto.getTopic())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写预约主题");
        }
        if (dto.getBookDate() == null || !StringUtils.hasText(dto.getTimeSlot())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择日期和时段");
        }
        Customer customer = resolveCustomer(dto, operatorId);
        customerAdmin.ensureActive(customer);
        CrmAppointment row = new CrmAppointment();
        row.setCustomerId(customer.getId());
        row.setVisitType(dto.getVisitType());
        row.setTopic(dto.getTopic().trim());
        row.setBookDate(dto.getBookDate());
        row.setTimeSlot(dto.getTimeSlot());
        row.setPlace(StringUtils.hasText(dto.getPlace()) ? dto.getPlace().trim() : null);
        row.setOwnerId(dto.getOwnerId() != null ? dto.getOwnerId() : operatorId);
        row.setStatus(CrmAppointment.PENDING);
        row.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        appointmentMapper.insert(row);
        return row;
    }

    public CrmAppointment changeStatus(Long id, String status) {
        if (!STATUSES.contains(status) || CrmAppointment.PENDING.equals(status)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "预约状态不正确");
        }
        CrmAppointment row = appointmentMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "预约不存在");
        }
        boolean open = CrmAppointment.PENDING.equals(row.getStatus()) || CrmAppointment.CONFIRMED.equals(row.getStatus());
        if (!open) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "这场预约已经结束");
        }
        if (CrmAppointment.DONE.equals(status) || CrmAppointment.NO_SHOW.equals(status)) {
            if (!CrmAppointment.CONFIRMED.equals(row.getStatus())) {
                throw new BizException(ErrorCode.OPERATION_FAILED, "请先确认预约");
            }
        }
        row.setStatus(status);
        appointmentMapper.updateById(row);
        return row;
    }

    private Customer resolveCustomer(AppointmentCreateDTO dto, Long operatorId) {
        if (dto.getCustomerId() != null) {
            return customerAdmin.must(dto.getCustomerId());
        }
        if (!StringUtils.hasText(dto.getPhone())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写客户手机号");
        }
        Customer found = customerAdmin.findByPhone(dto.getPhone().trim());
        if (found != null) {
            return found;
        }
        if (!Boolean.TRUE.equals(dto.getCreateIfAbsent())) {
            throw new BizException(ErrorCode.NOT_FOUND, "这个手机号还没有客户档案");
        }
        CustomerProfileDTO profile = new CustomerProfileDTO();
        profile.setName(dto.getName());
        profile.setPhone(dto.getPhone());
        profile.setSource("BOOKING");
        return customerAdmin.create(profile, operatorId);
    }

    private void fill(List<CrmAppointment> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> customerIds = rows.stream().map(CrmAppointment::getCustomerId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Customer> customers = new java.util.HashMap<>();
        if (!customerIds.isEmpty()) {
            customerMapper.selectBatchIds(customerIds).forEach(c -> customers.put(c.getId(), c));
        }
        Map<Long, String> names = staff.names(rows.stream().map(CrmAppointment::getOwnerId).filter(Objects::nonNull).toList());
        rows.forEach(row -> {
            Customer c = customers.get(row.getCustomerId());
            if (c != null) {
                row.setCustomerName(c.getName());
                row.setCustomerPhone(c.getPhone());
            }
            row.setOwnerName(names.get(row.getOwnerId()));
        });
    }

    private boolean contains(String value, String key) {
        return value != null && value.contains(key);
    }
}
