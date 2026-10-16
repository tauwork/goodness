package com.abos.server.module.booking.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.booking.entity.SvcBooking;
import com.abos.server.module.booking.mapper.SvcBookingMapper;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.mapper.SvcProductMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 服务预约：小程序提交 → 后台确认/完成/取消（含未转化归因）
 */
@Service
@RequiredArgsConstructor
public class SvcBookingService {

    private final SvcBookingMapper bookingMapper;
    private final SvcProductMapper productMapper;

    private static final Set<String> MODES = Set.of("VIDEO", "FUTIAN", "BAOAN", "KWAI_TSING");

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

    /** 后台列表：按日期/状态/关键词筛选，最近的在前 */
    public List<SvcBooking> adminList(LocalDate bookDate, String status, String keyword) {
        return bookingMapper.selectList(Wrappers.<SvcBooking>lambdaQuery()
                .eq(bookDate != null, SvcBooking::getBookDate, bookDate)
                .eq(StringUtils.hasText(status), SvcBooking::getStatus, status)
                .like(StringUtils.hasText(keyword), SvcBooking::getTopic, keyword)
                .orderByDesc(SvcBooking::getCreateTime));
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

    private void requireStatus(SvcBooking db, String... allowed) {
        for (String s : allowed) {
            if (s.equals(db.getStatus())) return;
        }
        throw new BizException(ErrorCode.ORDER_STATUS_INVALID,
                "当前状态 " + db.getStatus() + " 不允许该操作");
    }
}
