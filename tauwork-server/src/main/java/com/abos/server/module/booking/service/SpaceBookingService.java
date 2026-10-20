package com.abos.server.module.booking.service;

import cn.hutool.core.util.RandomUtil;
import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.booking.dto.BookingCreateDTO;
import com.abos.server.module.booking.entity.SpaceBooking;
import com.abos.server.module.booking.mapper.SpaceBookingMapper;
import com.abos.server.module.community.entity.CommunityProduct;
import com.abos.server.module.community.mapper.CommunityProductMapper;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.order.entity.Order;
import com.abos.server.module.order.entity.PayLog;
import com.abos.server.module.order.mapper.OrderMapper;
import com.abos.server.module.order.mapper.PayLogMapper;
import com.abos.server.module.order.pay.PayClient;
import com.abos.server.module.partner.service.PartnerService;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.mapper.SvcProductMapper;
import com.abos.server.security.AccessScope;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 空间预订核心流程：
 *  小程序下单 → 生成 SP 订单 + 预订单(待支付) → 拉起微信支付 → 支付回调置 PAID + 生成入场码
 *  后台运营：USED 核销 / CANCELED 退订(调退款)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceBookingService {

    private final SpaceBookingMapper bookingMapper;
    private final OrderMapper orderMapper;
    private final PayLogMapper payLogMapper;
    private final CommunityProductMapper communityProductMapper;
    private final SvcProductMapper productMapper;
    private final CustomerMapper customerMapper;
    private final PayClient payClient;
    private final AccessScope access;
    private final PartnerService partnerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /* ---------------- 小程序：下单 ---------------- */

    @Transactional
    public Map<String, Object> create(Long customerId, BookingCreateDTO dto) {
        CommunityProduct cp = communityProductMapper.selectById(dto.getCommunityProductId());
        if (cp == null || cp.getOnSale() != 1) {
            throw new BizException(ErrorCode.PRODUCT_NOT_ON_SALE, "该场地在本社区不可预订");
        }
        SvcProduct product = productMapper.selectById(cp.getProductId());
        if (product == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BizException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        if (dto.getBookDate().isBefore(LocalDate.now())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "预订日期不能早于今天");
        }

        // 计价：社区价优先；HOUR=单价×小时，其他=单价×数量
        BigDecimal unit = cp.getPrice() != null ? cp.getPrice() : product.getPriceValue();
        if (unit == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "该产品未配置价格（评估后报价类不适用在线预订）");
        }
        String unitType = cp.getPriceUnit() != null ? cp.getPriceUnit() : defaultUnit(product);
        int qty = dto.getQty() != null ? dto.getQty() : 1;
        BigDecimal amount;
        if ("HOUR".equals(unitType)) {
            if (dto.getStartTime() == null || dto.getHours() == null || dto.getHours() < 1) {
                throw new BizException(ErrorCode.BAD_REQUEST, "按小时计价须填写开始时间与时长");
            }
            amount = unit.multiply(BigDecimal.valueOf(dto.getHours()));
        } else {
            amount = unit.multiply(BigDecimal.valueOf(qty));
        }

        // 库存校验（限量产品）
        if (cp.getStock() != null && cp.getStock() <= 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "库存不足");
        }

        String orderNo = "SP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + RandomUtil.randomNumbers(4);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setType("SPACE");
        order.setCustomerId(customerId);
        order.setOpenid(customer.getWxOpenid());
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setAmount(amount);
        order.setPayStatus("UNPAID");
        order.setPayChannel("WECHAT");
        orderMapper.insert(order);

        SpaceBooking booking = new SpaceBooking();
        booking.setOrderId(order.getId());
        booking.setOrderNo(orderNo);
        booking.setCustomerId(customerId);
        booking.setCommunityId(cp.getCommunityId());
        booking.setCommunityProductId(cp.getId());
        booking.setBookDate(dto.getBookDate());
        booking.setStartTime(dto.getStartTime());
        booking.setHours(dto.getHours());
        booking.setQty("HOUR".equals(unitType) ? null : qty);
        booking.setAmount(amount);
        booking.setStatus("PENDING_PAY");
        bookingMapper.insert(booking);

        // 拉起微信支付（金额单位：分）
        Map<String, String> payParams = payClient.jsapiPay(orderNo,
                amount.multiply(BigDecimal.valueOf(100)),
                customer.getWxOpenid(), "空间预订-" + product.getName());
        logPay(orderNo, payParams.get("prepayId"), "PREPAY", true, String.valueOf(payParams));

        return Map.of("orderNo", orderNo, "amount", amount, "payParams", payParams);
    }

    /* ---------------- 支付回调（微信通知 / mock 确认共用） ---------------- */

    @Transactional
    public void handlePaySuccess(String orderNo) {
        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        if ("PAID".equals(order.getPayStatus())) {
            return; // 幂等
        }
        order.setPayStatus("PAID");
        orderMapper.updateById(order);

        SpaceBooking booking = bookingMapper.selectOne(Wrappers.<SpaceBooking>lambdaQuery()
                .eq(SpaceBooking::getOrderId, order.getId()));
        if (booking != null) {
            booking.setStatus("PAID");
            booking.setEntryCode(RandomUtil.randomNumbers(8));
            bookingMapper.updateById(booking);
            partnerService.accrue(order);
            logPay(orderNo, null, "NOTIFY", true, "entry_code=" + booking.getEntryCode());
        }
    }

    /** 开发模式模拟支付成功（正式环境走微信回调 /api/pay/notify/wx） */
    public void mockPayConfirm(String orderNo) {
        if (!payClient.isMock()) {
            throw new BizException(ErrorCode.FORBIDDEN, "正式支付环境请使用微信支付");
        }
        handlePaySuccess(orderNo);
    }

    /* ---------------- 小程序：我的预订 ---------------- */

    public List<SpaceBooking> myBookings(Long customerId) {
        return bookingMapper.selectList(Wrappers.<SpaceBooking>lambdaQuery()
                .eq(SpaceBooking::getCustomerId, customerId)
                .orderByDesc(SpaceBooking::getCreateTime));
    }

    /* ---------------- 后台：订单状态流转 ---------------- */

    /** 运营操作：USED 已使用（核销）/ CANCELED 已退订（退款） */
    @Transactional
    public SpaceBooking changeStatus(Long bookingId, String status, Long operatorId) {
        SpaceBooking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND, "预订单不存在");
        }
        Order order = orderMapper.selectById(booking.getOrderId());

        switch (status) {
            case "USED" -> {
                requireStatus(booking, "PAID");
                booking.setStatus("USED");
                booking.setUsedAt(LocalDateTime.now());
            }
            case "CANCELED" -> {
                requireStatus(booking, "PENDING_PAY", "PAID");
                booking.setStatus("CANCELED");
                // 已支付的走退款
                if (order != null && "PAID".equals(order.getPayStatus())) {
                    payClient.refund(booking.getOrderNo(), null,
                            booking.getAmount().multiply(BigDecimal.valueOf(100)));
                    order.setPayStatus("REFUNDED");
                    orderMapper.updateById(order);
                    partnerService.reverseOrder(order.getId());
                    logPay(booking.getOrderNo(), null, "REFUND", true, "operator=" + operatorId);
                }
            }
            default -> throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "仅支持 USED/CANCELED");
        }
        bookingMapper.updateById(booking);
        return booking;
    }

    /** 后台列表：按社区店/日期/状态筛选 */
    public List<SpaceBooking> adminList(Long communityId, LocalDate bookDate, String status) {
        AccessScope.View scope = access.current();
        if (!scope.all() && scope.communityIds().isEmpty()) {
            return List.of();
        }
        return bookingMapper.selectList(Wrappers.<SpaceBooking>lambdaQuery()
                .eq(communityId != null, SpaceBooking::getCommunityId, communityId)
                .in(!scope.all(), SpaceBooking::getCommunityId, scope.communityIds())
                .eq(bookDate != null, SpaceBooking::getBookDate, bookDate)
                .eq(status != null, SpaceBooking::getStatus, status)
                .orderByDesc(SpaceBooking::getCreateTime));
    }

    private void requireStatus(SpaceBooking booking, String... allowed) {
        for (String s : allowed) {
            if (s.equals(booking.getStatus())) return;
        }
        throw new BizException(ErrorCode.ORDER_STATUS_INVALID,
                "当前状态 " + booking.getStatus() + " 不允许该操作");
    }

    private String defaultUnit(SvcProduct product) {
        // 未配社区级单位时按分类粗略缺省：会议室=HOUR，其余=ONCE
        return product.getCategoryId() != null && product.getCategoryId() == 203L ? "HOUR" : "ONCE";
    }

    private void logPay(String orderNo, String prepayId, String event, boolean success, String raw) {
        try {
            PayLog payLog = new PayLog();
            payLog.setOrderNo(orderNo);
            payLog.setPrepayId(prepayId);
            payLog.setEvent(event);
            payLog.setSuccess(success ? 1 : 0);
            payLog.setRaw(objectMapper.writeValueAsString(raw));
            payLogMapper.insert(payLog);
        } catch (Exception e) {
            log.warn("支付流水记录失败: {}", e.getMessage());
        }
    }
}
