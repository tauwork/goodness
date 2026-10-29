package com.abos.server.module.svcorder.service;

import cn.hutool.core.util.RandomUtil;
import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.file.config.UploadProperties;
import com.abos.server.module.order.entity.Order;
import com.abos.server.module.order.entity.PayLog;
import com.abos.server.module.order.mapper.OrderMapper;
import com.abos.server.module.order.mapper.PayLogMapper;
import com.abos.server.module.order.pay.PayClient;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.mapper.SvcProductMapper;
import com.abos.server.module.finance.service.FinanceAccountService;
import com.abos.server.module.coreg.service.CoRegService;
import com.abos.server.module.svcorder.dto.SvcOrderCreateDTO;
import com.abos.server.module.svcorder.entity.SvcOrder;
import com.abos.server.module.svcorder.mapper.SvcOrderMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务下单。在线支付走微信支付；对公转账先落待支付订单，上传凭证后等待确认到账。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SvcOrderService {

    private final SvcOrderMapper svcOrderMapper;
    private final OrderMapper orderMapper;
    private final PayLogMapper payLogMapper;
    private final SvcProductMapper productMapper;
    private final CustomerMapper customerMapper;
    private final PayClient payClient;
    private final FinanceAccountService financeAccountService;
    private final CoRegService coRegService;
    private final UploadProperties uploadProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Map<String, Object> create(Long customerId, SvcOrderCreateDTO dto) {
        String channel = dto.getPayChannel() == null ? "" : dto.getPayChannel().trim();
        if (!SvcOrder.WECHAT.equals(channel) && !SvcOrder.TRANSFER.equals(channel)) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "请选择在线支付或对公转账");
        }
        SvcProduct product = productMapper.selectById(dto.getProductId());
        if (product == null || !SvcProduct.TYPE_SERVICE.equals(product.getProdType())) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        if (product.getOnSale() == null || product.getOnSale() != 1) {
            throw new BizException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }
        if (product.getPriceValue() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "服务未标价");
        }
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BizException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        if (SvcOrder.WECHAT.equals(channel) && !StringUtils.hasText(customer.getWxOpenid()) && !payClient.isMock()) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "请先使用微信登录后再支付");
        }

        BigDecimal amount = product.getPriceValue();
        String orderNo = "SV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + RandomUtil.randomNumbers(4);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setType("SERVICE");
        order.setCustomerId(customerId);
        order.setOpenid(customer.getWxOpenid());
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setAmount(amount);
        order.setPayStatus("UNPAID");
        order.setPayChannel(channel);
        orderMapper.insert(order);

        SvcOrder svcOrder = new SvcOrder();
        svcOrder.setOrderId(order.getId());
        svcOrder.setOrderNo(orderNo);
        svcOrder.setCustomerId(customerId);
        svcOrder.setProductId(product.getId());
        svcOrder.setProductName(product.getName());
        svcOrder.setCoverUrl(product.getCoverUrl());
        svcOrder.setAmount(amount);
        svcOrder.setPayChannel(channel);
        svcOrder.setStatus(SvcOrder.PENDING_PAY);
        svcOrderMapper.insert(svcOrder);
        coRegService.open(svcOrder, product);

        Map<String, Object> result = baseView(svcOrder);
        if (SvcOrder.WECHAT.equals(channel)) {
            Map<String, String> payParams = payClient.jsapiPay(orderNo,
                    amount.multiply(BigDecimal.valueOf(100)),
                    customer.getWxOpenid(), "服务-" + product.getName());
            logPay(orderNo, payParams.get("prepayId"), "PREPAY", true, String.valueOf(payParams));
            result.put("payParams", payParams);
            result.put("mock", payClient.isMock());
        } else {
            result.put("account", account());
        }
        return result;
    }

    public Map<String, Object> repay(Long customerId, String orderNo) {
        SvcOrder svcOrder = owned(customerId, orderNo);
        if (!SvcOrder.WECHAT.equals(svcOrder.getPayChannel())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID.getCode(), "这笔订单不是在线支付");
        }
        if (!SvcOrder.PENDING_PAY.equals(svcOrder.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID.getCode(), "订单不是待支付");
        }
        Order order = orderMapper.selectById(svcOrder.getOrderId());
        Map<String, String> payParams = payClient.jsapiPay(orderNo,
                svcOrder.getAmount().multiply(BigDecimal.valueOf(100)),
                order == null ? null : order.getOpenid(), "服务-" + svcOrder.getProductName());
        Map<String, Object> result = baseView(svcOrder);
        result.put("payParams", payParams);
        result.put("mock", payClient.isMock());
        return result;
    }

    @Transactional
    public void handlePaySuccess(String orderNo) {
        Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        if ("PAID".equals(order.getPayStatus()) || "REFUNDED".equals(order.getPayStatus())) {
            return;
        }
        SvcOrder svcOrder = byOrderNo(orderNo);
        if (SvcOrder.CANCELED.equals(svcOrder.getStatus()) || SvcOrder.PAID.equals(svcOrder.getStatus())) {
            return;
        }
        order.setPayStatus("PAID");
        orderMapper.updateById(order);
        svcOrder.setStatus(SvcOrder.PAID);
        svcOrderMapper.updateById(svcOrder);
        logPay(orderNo, null, "NOTIFY", true, "service paid");
    }

    @Transactional
    public void mockPay(Long customerId, String orderNo) {
        if (!payClient.isMock()) {
            throw new BizException(ErrorCode.FORBIDDEN.getCode(), "正式支付环境请使用微信支付");
        }
        SvcOrder svcOrder = owned(customerId, orderNo);
        if (!SvcOrder.WECHAT.equals(svcOrder.getPayChannel())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID.getCode(), "对公转账请上传凭证");
        }
        handlePaySuccess(orderNo);
    }

    @Transactional
    public SvcOrder submitVoucher(Long customerId, String orderNo, String url) {
        SvcOrder svcOrder = owned(customerId, orderNo);
        if (!SvcOrder.TRANSFER.equals(svcOrder.getPayChannel())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID.getCode(), "只有对公转账需要上传凭证");
        }
        if (!SvcOrder.PENDING_PAY.equals(svcOrder.getStatus()) && !SvcOrder.CONFIRMING.equals(svcOrder.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID.getCode(), "当前状态不能上传凭证");
        }
        String prefix = uploadProperties.getUrlPrefix();
        if (!StringUtils.hasText(url) || !url.startsWith(prefix)) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "凭证地址无效");
        }
        svcOrder.setVoucherUrl(url);
        svcOrder.setStatus(SvcOrder.CONFIRMING);
        svcOrderMapper.updateById(svcOrder);
        return svcOrder;
    }

    @Transactional
    public void cancelByCustomer(Long customerId, String orderNo) {
        SvcOrder svcOrder = owned(customerId, orderNo);
        if (!SvcOrder.PENDING_PAY.equals(svcOrder.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID.getCode(), "只有待支付订单可以取消");
        }
        svcOrder.setStatus(SvcOrder.CANCELED);
        svcOrderMapper.updateById(svcOrder);
        Order order = orderMapper.selectById(svcOrder.getOrderId());
        if (order != null && "UNPAID".equals(order.getPayStatus())) {
            order.setPayStatus("CANCELED");
            orderMapper.updateById(order);
        }
        coRegService.cancel(orderNo);
    }

    @Transactional
    public SvcOrder confirm(Long id) {
        SvcOrder svcOrder = svcOrderMapper.selectById(id);
        if (svcOrder == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!SvcOrder.CONFIRMING.equals(svcOrder.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID.getCode(), "只有待确认到账的订单可以确认");
        }
        handlePaySuccess(svcOrder.getOrderNo());
        return svcOrderMapper.selectById(id);
    }

    public List<SvcOrder> myOrders(Long customerId) {
        return svcOrderMapper.selectList(Wrappers.<SvcOrder>lambdaQuery()
                .eq(SvcOrder::getCustomerId, customerId)
                .orderByDesc(SvcOrder::getCreateTime));
    }

    public Map<String, Object> mine(Long customerId, String orderNo) {
        SvcOrder svcOrder = owned(customerId, orderNo);
        Map<String, Object> view = baseView(svcOrder);
        if (SvcOrder.TRANSFER.equals(svcOrder.getPayChannel())) {
            view.put("account", account());
        }
        return view;
    }

    public List<SvcOrder> adminList(String status) {
        return svcOrderMapper.selectList(Wrappers.<SvcOrder>lambdaQuery()
                .eq(StringUtils.hasText(status), SvcOrder::getStatus, status)
                .orderByDesc(SvcOrder::getCreateTime));
    }

    public Map<String, String> account() {
        return financeAccountService.view();
    }

    private Map<String, Object> baseView(SvcOrder svcOrder) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", svcOrder.getId());
        view.put("orderNo", svcOrder.getOrderNo());
        view.put("productId", svcOrder.getProductId());
        view.put("productName", svcOrder.getProductName());
        view.put("coverUrl", svcOrder.getCoverUrl());
        view.put("amount", svcOrder.getAmount());
        view.put("payChannel", svcOrder.getPayChannel());
        view.put("status", svcOrder.getStatus());
        view.put("voucherUrl", svcOrder.getVoucherUrl());
        view.put("createTime", svcOrder.getCreateTime());
        return view;
    }

    private SvcOrder owned(Long customerId, String orderNo) {
        SvcOrder svcOrder = byOrderNo(orderNo);
        if (!svcOrder.getCustomerId().equals(customerId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return svcOrder;
    }

    private SvcOrder byOrderNo(String orderNo) {
        SvcOrder svcOrder = svcOrderMapper.selectOne(Wrappers.<SvcOrder>lambdaQuery()
                .eq(SvcOrder::getOrderNo, orderNo));
        if (svcOrder == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        return svcOrder;
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
