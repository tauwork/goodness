package com.abos.server.module.order.pay;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付客户端抽象：微信支付 V3（JSAPI 小程序下单）
 * 实现：WxPayV3Client（正式，需商户号配置）/ MockPayClient（开发联调）
 */
public interface PayClient {

    /** JSAPI 下单：返回小程序 wx.requestOrderPayment 所需参数 */
    Map<String, String> jsapiPay(String orderNo, BigDecimal amountFen, String openid, String description);

    /** 退款（已支付订单退订时调用） */
    void refund(String orderNo, String transactionId, BigDecimal refundFen);

    /** 校验支付回调签名并返回订单号；验签失败抛异常 */
    String parseNotify(byte[] body, Map<String, String> headers);

    boolean isMock();
}
