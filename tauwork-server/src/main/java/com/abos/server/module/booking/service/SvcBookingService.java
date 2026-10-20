package com.abos.server.module.booking.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.booking.dto.SvcBookStaffDTO;
import com.abos.server.module.booking.entity.SvcBooking;
import com.abos.server.module.booking.mapper.SvcBookingMapper;
import com.abos.server.module.customer.dto.CustomerProfileDTO;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.customer.service.CustomerAdminService;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.mapper.SvcProductMapper;
import com.abos.server.module.sysuser.service.SysUserService;
import com.abos.server.security.AccessScope;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 服务预约：小程序提交 → 后台确认/完成/取消（含未转化归因）
 */
@Service
@RequiredArgsConstructor
public class SvcBookingService {

    private final SvcBookingMapper bookingMapper;
    private final SvcProductMapper productMapper;
    private final CustomerMapper customerMapper;
    private final CustomerAdminService customerAdmin;
    private final SysUserService staff;
    private final AccessScope access;

    private static final Set<String> MODES = Set.of("VIDEO", "FUTIAN", "BAOAN", "KWAI_TSING");
    public static final List<String> SLOTS = List.of("09:30", "10:30", "14:00", "15:30", "16:30", "19:00");

    public SvcBooking create(Long customerId, SvcBooking dto) {
        if (!MODES.contains(dto.getMode())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "咨询方式不合法");
        }
        if (dto.getBookDate() == null || dto.getBookDate().isBefore(LocalDate.now())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "预约日期不能早于今天");
        }
        if (!StringUtils.hasText(dto.getTimeSlot())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择预约时段");
        }
        if (!StringUtils.hasText(dto.getTopic())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写咨询主题");
        }
        if (dto.getProductId() != null) {
            SvcProduct p = productMapper.selectById(dto.getProductId());
            if (p == null) throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
            dto.setProductName(p.getName());
        }
        dto.setId(null);
        dto.setCustomerId(customerId);
        dto.setStatus("PENDING");
        bookingMapper.insert(dto);
        return dto;
    }

    public List<SvcBooking> myList(Long customerId) {
        return bookingMapper.selectList(Wrappers.<SvcBooking>lambdaQuery()
                .eq(SvcBooking::getCustomerId, customerId)
                .orderByDesc(SvcBooking::getBookDate));
    }

    /** 后台列表：和小程序提交的是同一批咨询预约，按归属社区收窄到店方式。 */
    public List<SvcBooking> adminList(LocalDate bookDate, String status, String keyword) {
        AccessScope.View scope = access.current();
        List<SvcBooking> rows = bookingMapper.selectList(Wrappers.<SvcBooking>lambdaQuery()
                .eq(bookDate != null, SvcBooking::getBookDate, bookDate)
                .eq(StringUtils.hasText(status), SvcBooking::getStatus, status)
                .in(!scope.all(), SvcBooking::getMode, scope.bookingModes())
                .orderByDesc(SvcBooking::getBookDate)
                .orderByAsc(SvcBooking::getTimeSlot));
        fill(rows);
        if (!StringUtils.hasText(keyword)) {
            return rows;
        }
        String key = keyword.trim();
        return rows.stream().filter(row -> contains(row.getTopic(), key)
                || contains(row.getProductName(), key)
                || contains(row.getCustomerName(), key)
                || contains(row.getCustomerPhone(), key)).toList();
    }

    /** 顾问代客登记，写入与小程序相同的咨询预约。 */
    @Transactional
    public SvcBooking createByStaff(SvcBookStaffDTO dto, Long operatorId) {
        if (!MODES.contains(dto.getMode())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择咨询方式");
        }
        if (dto.getBookDate() == null || dto.getBookDate().isBefore(LocalDate.now())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "预约日期不能早于今天");
        }
        if (!SLOTS.contains(dto.getTimeSlot())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择预约时段");
        }
        Customer customer = resolveCustomer(dto, operatorId);
        customerAdmin.ensureActive(customer);
        SvcBooking row = new SvcBooking();
        row.setCustomerId(customer.getId());
        row.setMode(dto.getMode());
        row.setBookDate(dto.getBookDate());
        row.setTimeSlot(dto.getTimeSlot());
        row.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        row.setConsultantId(operatorId);
        row.setStatus("PENDING");
        if (dto.getProductId() != null) {
            SvcProduct product = productMapper.selectById(dto.getProductId());
            if (product == null || !SvcProduct.TYPE_SERVICE.equals(product.getProdType())) {
                throw new BizException(ErrorCode.PRODUCT_NOT_FOUND, "请选择服务项目");
            }
            row.setProductId(product.getId());
            row.setProductName(product.getName());
        }
        String topic = StringUtils.hasText(dto.getTopic()) ? dto.getTopic().trim() : row.getProductName();
        if (!StringUtils.hasText(topic)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写咨询主题");
        }
        row.setTopic(topic);
        bookingMapper.insert(row);
        return row;
    }

    /** 状态流转：CONFIRMED 确认 / DONE 完成 / CANCELED 取消 / NO_SHOW 爽约 */
    public SvcBooking changeStatus(Long id, String status, String failReason) {
        SvcBooking db = bookingMapper.selectById(id);
        if (db == null) throw new BizException(ErrorCode.NOT_FOUND, "预约不存在");
        if (!Set.of("CONFIRMED", "DONE", "CANCELED", "NO_SHOW").contains(status)) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "仅支持 CONFIRMED/DONE/CANCELED/NO_SHOW");
        }
        switch (status) {
            case "CONFIRMED" -> requireStatus(db, "PENDING");
            case "DONE", "NO_SHOW" -> requireStatus(db, "CONFIRMED");
            case "CANCELED" -> requireStatus(db, "PENDING", "CONFIRMED");
        }
        db.setStatus(status);
        if ("NO_SHOW".equals(status) || "CANCELED".equals(status)) {
            db.setFailReason(StringUtils.hasText(failReason) ? failReason : null);
        }
        bookingMapper.updateById(db);
        return db;
    }

    private Customer resolveCustomer(SvcBookStaffDTO dto, Long operatorId) {
        if (dto.getCustomerId() != null) {
            Customer found = customerMapper.selectById(dto.getCustomerId());
            if (found != null) {
                return found;
            }
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

    private void fill(List<SvcBooking> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> customerIds = rows.stream().map(SvcBooking::getCustomerId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Customer> customers = new HashMap<>();
        if (!customerIds.isEmpty()) {
            customerMapper.selectBatchIds(customerIds).forEach(c -> customers.put(c.getId(), c));
        }
        Map<Long, String> names = staff.names(rows.stream().map(SvcBooking::getConsultantId).filter(Objects::nonNull).toList());
        rows.forEach(row -> {
            Customer customer = row.getCustomerId() == null ? null : customers.get(row.getCustomerId());
            if (customer != null) {
                row.setCustomerName(StringUtils.hasText(customer.getName()) ? customer.getName() : "未命名");
                row.setCustomerPhone(customer.getPhone());
            }
            if (row.getConsultantId() != null) {
                row.setConsultantName(names.get(row.getConsultantId()));
            }
        });
    }

    private boolean contains(String value, String key) {
        return value != null && value.contains(key);
    }

    private void requireStatus(SvcBooking db, String... allowed) {
        for (String s : allowed) {
            if (s.equals(db.getStatus())) return;
        }
        throw new BizException(ErrorCode.ORDER_STATUS_INVALID,
                "当前状态 " + db.getStatus() + " 不允许该操作");
    }
}
