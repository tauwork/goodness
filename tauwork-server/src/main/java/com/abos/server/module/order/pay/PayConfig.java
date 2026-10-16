package com.abos.server.module.order.pay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 支付配置与客户端装配：
 *  wx.pay.enabled=false（或商户号未配）→ 自动使用 MockPayClient，前端可走"模拟支付成功"
 *  正式环境配置 appid/mchid/serial/private-key-path/api-v3-key 后切换为 WxPayV3Client
 */
@Slf4j
@Configuration
public class PayConfig {

    @Bean
    @ConfigurationProperties(prefix = "wx.pay")
    public WxPayProperties wxPayProperties() {
        return new WxPayProperties();
    }

    @Bean
    public PayClient payClient(WxPayProperties props) {
        if (props.isEnabled() && props.isReady()) {
            log.info("支付客户端: 微信支付V3 (mchid={})", props.getMchid());
            return new WxPayV3Client(props);
        }
        log.warn("支付客户端: Mock（未配置微信商户号，wx.pay.enabled={}）", props.isEnabled());
        return new MockPayClient();
    }

    /** 微信支付 V3 配置项 */
    public static class WxPayProperties {
        private boolean enabled = false;
        private String appid;
        private String mchid;
        private String serial;
        private String privateKeyPath;
        private String apiV3Key;
        private String notifyUrl;

        public boolean isReady() {
            return appid != null && mchid != null && serial != null
                    && privateKeyPath != null && apiV3Key != null && notifyUrl != null;
        }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getAppid() { return appid; }
        public void setAppid(String appid) { this.appid = appid; }
        public String getMchid() { return mchid; }
        public void setMchid(String mchid) { this.mchid = mchid; }
        public String getSerial() { return serial; }
        public void setSerial(String serial) { this.serial = serial; }
        public String getPrivateKeyPath() { return privateKeyPath; }
        public void setPrivateKeyPath(String privateKeyPath) { this.privateKeyPath = privateKeyPath; }
        public String getApiV3Key() { return apiV3Key; }
        public void setApiV3Key(String apiV3Key) { this.apiV3Key = apiV3Key; }
        public String getNotifyUrl() { return notifyUrl; }
        public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }
    }

    /** Mock 实现：返回模拟支付参数；notify 直接透传订单号 */
    static class MockPayClient implements PayClient {
        @Override
        public Map<String, String> jsapiPay(String orderNo, BigDecimal amountFen, String openid, String description) {
            Map<String, String> params = new HashMap<>();
            params.put("mock", "true");
            params.put("orderNo", orderNo);
            params.put("prepayId", "MOCK_" + UUID.randomUUID().toString().substring(0, 8));
            return params;
        }

        @Override
        public void refund(String orderNo, String transactionId, BigDecimal refundFen) { }

        @Override
        public String parseNotify(byte[] body, Map<String, String> headers) {
            // Mock 回调报文：JSON {"orderNo":"SP-xxx"}
            String s = new String(body, java.nio.charset.StandardCharsets.UTF_8);
            return s.replaceAll(".*\"orderNo\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        }

        @Override
        public boolean isMock() { return true; }
    }
}
