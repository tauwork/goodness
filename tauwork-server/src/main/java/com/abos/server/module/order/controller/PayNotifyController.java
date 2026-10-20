package com.abos.server.module.order.controller;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.booking.service.SpaceBookingService;
import com.abos.server.module.goods.service.GoodsOrderService;
import com.abos.server.module.order.entity.Order;
import com.abos.server.module.order.mapper.OrderMapper;
import com.abos.server.module.order.pay.PayClient;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信支付回调（公开）：POST /api/pay/notify/wx
 * 微信 V3 回调要求返回 {"code":"SUCCESS"}；验签失败返回 4xx 由微信重试
 */
@Slf4j
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayNotifyController {

    private final PayClient payClient;
    private final SpaceBookingService bookingService;
    private final GoodsOrderService goodsOrderService;
    private final OrderMapper orderMapper;

    @PostMapping("/notify/wx")
    public ResponseEntity<Map<String, String>> wxNotify(@RequestBody byte[] body,
                                                        @RequestHeader Map<String, String> headers) {
        try {
            String orderNo = payClient.parseNotify(body, headers);
            Order order = orderMapper.selectOne(Wrappers.<Order>lambdaQuery().eq(Order::getOrderNo, orderNo));
            if (order == null) {
                throw new BizException(ErrorCode.ORDER_NOT_FOUND);
            }
            if ("GOODS".equals(order.getType())) {
                goodsOrderService.handlePaySuccess(orderNo);
            } else {
                bookingService.handlePaySuccess(orderNo);
            }
            return ResponseEntity.ok(Map.of("code", "SUCCESS", "message", "成功"));
        } catch (Exception e) {
            log.error("微信回调处理失败", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("code", "FAIL", "message", e.getMessage()));
        }
    }
}
