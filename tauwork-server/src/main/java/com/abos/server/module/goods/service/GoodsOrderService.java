package com.abos.server.module.goods.service;

import cn.hutool.core.util.RandomUtil;
import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.goods.dto.GoodsOrderCreateDTO;
import com.abos.server.module.goods.entity.GoodsOrder;
import com.abos.server.module.goods.mapper.GoodsOrderMapper;
import com.abos.server.module.order.entity.Order;
import com.abos.server.module.order.entity.PayLog;
import com.abos.server.module.order.mapper.OrderMapper;
import com.abos.server.module.order.mapper.PayLogMapper;
import com.abos.server.module.order.pay.PayClient;
import com.abos.server.module.partner.service.PartnerService;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.mapper.SvcProductMapper;
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
 * 通用商品下单：下单即占库存，支付成功后待交付，取消或退款退回库存。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsOrderService {

    private final GoodsOrderMapper goodsOrderMapper;
    private final OrderMapper orderMapper;
    private final PayLogMapper payLogMapper;
    private final SvcProductMapper productMapper;
    private final CustomerMapper customerMapper;
    private final PayClient payClient;
    private final PartnerService partnerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Map<String, Object> create(Long customerId, GoodsOrderCreateDTO dto) {
        SvcProduct product = productMapper.selectById(dto.getProductId());
        if (product == null || !SvcProduct.TYPE_GOODS.equals(product.getProdType())) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在");
        }
        if (product.getOnSale() == null || product.getOnSale() != 1) {
            throw new BizException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }
        if (product.getPriceValue() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "商品未标价");
        }
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BizException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
        int qty = dto.getQty();
        int taken = productMapper.update(null, Wrappers.<SvcProduct>lambdaUpdate()
                .setSql("stock = stock - " + qty)
                .eq(SvcProduct::getId, product.getId())
                .eq(SvcProduct::getOnSale, 1)
                .ge(SvcProduct::getStock, qty));
        if (taken == 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "库存不足");
        }

        BigDecimal amount = product.getPriceValue().multiply(BigDecimal.valueOf(qty));
        String orderNo = "GD-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + RandomUtil.randomNumbers(4);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setType("GOODS");
        order.setCustomerId(customerId);
        order.setOpenid(customer.getWxOpenid());
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setAmount(amount);
        order.setPayStatus("UNPAID");
        order.setPayChannel("WECHAT");
        orderMapper.insert(order);

        GoodsOrder goodsOrder = new GoodsOrder();
        goodsOrder.setOrderId(order.getId());
        goodsOrder.setOrderNo(orderNo);
        goodsOrder.setCustomerId(customerId);
        goodsOrder.setProductId(product.getId());
        goodsOrder.setProductName(product.getName());
        goodsOrder.setSpec(product.getSpec());
        goodsOrder.setCoverUrl(product.getCoverUrl());
        goodsOrder.setQty(qty);
        goodsOrder.setUnitPrice(product.getPriceValue());
        goodsOrder.setAmount(amount);
        goodsOrder.setRemark(trimRemark(dto.getRemark()));
        goodsOrder.setStatus(GoodsOrder.PENDING_PAY);
        goodsOrderMapper.insert(goodsOrder);

        Map<String, String> payParams = payClient.jsapiPay(orderNo,
                amount.multiply(BigDecimal.valueOf(100)),
                customer.getWxOpenid(), "商品-" + product.getName());
        logPay(orderNo, payParams.get("prepayId"), "PREPAY", true, String.valueOf(payParams));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderNo", orderNo);
        result.put("amount", amount);
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
        GoodsOrder goodsOrder = byOrderNo(orderNo);
        if (GoodsOrder.CANCELED.equals(goodsOrder.getStatus())) {
            return;
        }
        order.setPayStatus("PAID");
        orderMapper.updateById(order);
        goodsOrder.setStatus(GoodsOrder.PAID);
        goodsOrderMapper.updateById(goodsOrder);
        partnerService.accrue(order);
        logPay(orderNo, null, "NOTIFY", true, "goods paid");
    }

    public void mockPay(Long customerId, String orderNo) {
        if (!payClient.isMock()) {
            throw new BizException(ErrorCode.FORBIDDEN, "正式支付环境请使用微信支付");
        }
        GoodsOrder goodsOrder = byOrderNo(orderNo);
        if (!goodsOrder.getCustomerId().equals(customerId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        handlePaySuccess(orderNo);
    }

    @Transactional
    public void cancelByCustomer(Long customerId, String orderNo) {
        GoodsOrder goodsOrder = byOrderNo(orderNo);
        if (!goodsOrder.getCustomerId().equals(customerId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (!GoodsOrder.PENDING_PAY.equals(goodsOrder.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "只有待支付订单可以取消");
        }
        cancel(goodsOrder, null);
    }

    public List<GoodsOrder> myOrders(Long customerId) {
        return goodsOrderMapper.selectList(Wrappers.<GoodsOrder>lambdaQuery()
                .eq(GoodsOrder::getCustomerId, customerId)
                .orderByDesc(GoodsOrder::getCreateTime));
    }

    public List<GoodsOrder> adminList(String status) {
        return goodsOrderMapper.selectList(Wrappers.<GoodsOrder>lambdaQuery()
                .eq(StringUtils.hasText(status), GoodsOrder::getStatus, status)
                .orderByDesc(GoodsOrder::getCreateTime));
    }

    @Transactional
    public GoodsOrder changeStatus(Long id, String status) {
        GoodsOrder goodsOrder = goodsOrderMapper.selectById(id);
        if (goodsOrder == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND, "商品订单不存在");
        }
        if (status.equals(goodsOrder.getStatus())) {
            return goodsOrder;
        }
        switch (status) {
            case GoodsOrder.PAID -> {
                if (GoodsOrder.DONE.equals(goodsOrder.getStatus())) {
                    goodsOrder.setStatus(GoodsOrder.PAID);
                    goodsOrderMapper.updateById(goodsOrder);
                } else {
                    require(goodsOrder, GoodsOrder.PENDING_PAY);
                    handlePaySuccess(goodsOrder.getOrderNo());
                }
            }
            case GoodsOrder.DONE -> {
                require(goodsOrder, GoodsOrder.PAID);
                goodsOrder.setStatus(GoodsOrder.DONE);
                goodsOrderMapper.updateById(goodsOrder);
            }
            case GoodsOrder.CANCELED -> cancel(goodsOrder, null);
            default -> throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "不能改成这个状态");
        }
        return goodsOrderMapper.selectById(id);
    }

    private void cancel(GoodsOrder goodsOrder, Long operatorId) {
        require(goodsOrder, GoodsOrder.PENDING_PAY, GoodsOrder.PAID, GoodsOrder.DONE);
        Order order = orderMapper.selectById(goodsOrder.getOrderId());
        boolean paid = order != null && "PAID".equals(order.getPayStatus());
        goodsOrder.setStatus(GoodsOrder.CANCELED);
        goodsOrderMapper.updateById(goodsOrder);
        restoreStock(goodsOrder.getProductId(), goodsOrder.getQty());
        if (paid) {
            payClient.refund(goodsOrder.getOrderNo(), null,
                    goodsOrder.getAmount().multiply(BigDecimal.valueOf(100)));
            order.setPayStatus("REFUNDED");
            orderMapper.updateById(order);
            partnerService.reverseOrder(order.getId());
            logPay(goodsOrder.getOrderNo(), null, "REFUND", true, "operator=" + operatorId);
        }
    }

    private void restoreStock(Long productId, int qty) {
        productMapper.update(null, Wrappers.<SvcProduct>lambdaUpdate()
                .setSql("stock = IFNULL(stock,0) + " + qty)
                .eq(SvcProduct::getId, productId));
    }

    private GoodsOrder byOrderNo(String orderNo) {
        GoodsOrder goodsOrder = goodsOrderMapper.selectOne(Wrappers.<GoodsOrder>lambdaQuery()
                .eq(GoodsOrder::getOrderNo, orderNo));
        if (goodsOrder == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        }
        return goodsOrder;
    }

    private void require(GoodsOrder goodsOrder, String... allowed) {
        for (String status : allowed) {
            if (status.equals(goodsOrder.getStatus())) {
                return;
            }
        }
        throw new BizException(ErrorCode.ORDER_STATUS_INVALID,
                "当前状态 " + goodsOrder.getStatus() + " 不允许该操作");
    }

    private String trimRemark(String remark) {
        if (!StringUtils.hasText(remark)) {
            return null;
        }
        String text = remark.trim();
        return text.length() > 255 ? text.substring(0, 255) : text;
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
