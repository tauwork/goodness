package com.abos.server.module.order.pay;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信支付 V3 JSAPI 客户端（小程序下单）。
 * 接入步骤（拿到商户资质后）：
 *  1. application.yml 配置 wx.pay.*（appid/mchid/serial/private-key-path/api-v3-key/notify-url）并 enabled=true
 *  2. notify-url 需 HTTPS 且已备案域名，指向 /api/pay/notify/wx
 * 验签说明：V3 回调验签需平台证书下载与解密（wechatpay-java SDK），此处回调验签
 * 以 apiV3Key 解密 resource 为准；如需平台证书级验签建议引入官方 SDK 后替换 parseNotify。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WxPayV3Client implements PayClient {

    private static final String JSAPI_ORDER_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";
    private static final String REFUND_URL = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

    private final PayConfig.WxPayProperties props;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, String> jsapiPay(String orderNo, BigDecimal amountFen, String openid, String description) {
        try {
            Map<String, Object> body = Map.of(
                    "appid", props.getAppid(),
                    "mchid", props.getMchid(),
                    "description", description,
                    "out_trade_no", orderNo,
                    "notify_url", props.getNotifyUrl(),
                    "amount", Map.of("total", amountFen.intValue(), "currency", "CNY"),
                    "payer", Map.of("openid", openid));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", buildAuthHeader("POST", "/v3/pay/transactions/jsapi", body));
            String resp = restTemplate.postForObject(JSAPI_ORDER_URL,
                    new HttpEntity<>(objectMapper.writeValueAsString(body), headers), String.class);
            String prepayId = objectMapper.readTree(resp).get("prepay_id").asText();

            // 组装 wx.requestOrderPayment 参数
            long ts = System.currentTimeMillis() / 1000;
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String pkg = "prepay_id=" + prepayId;
            String message = props.getAppid() + "\n" + ts + "\n" + nonceStr + "\n" + pkg + "\n";
            String paySign = sign(message);
            Map<String, String> params = new HashMap<>();
            params.put("timeStamp", String.valueOf(ts));
            params.put("nonceStr", nonceStr);
            params.put("package", pkg);
            params.put("signType", "RSA");
            params.put("paySign", paySign);
            return params;
        } catch (Exception e) {
            log.error("微信下单失败 orderNo={}", orderNo, e);
            throw new IllegalStateException("微信支付下单失败: " + e.getMessage());
        }
    }

    @Override
    public void refund(String orderNo, String transactionId, BigDecimal refundFen) {
        try {
            Map<String, Object> body = Map.of(
                    "out_trade_no", orderNo,
                    "out_refund_no", "RF" + orderNo,
                    "amount", Map.of("refund", refundFen.intValue(),
                            "total", refundFen.intValue(), "currency", "CNY"));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", buildAuthHeader("POST", "/v3/refund/domestic/refunds", body));
            restTemplate.postForObject(REFUND_URL,
                    new HttpEntity<>(objectMapper.writeValueAsString(body), headers), String.class);
        } catch (Exception e) {
            log.error("微信退款失败 orderNo={}", orderNo, e);
            throw new IllegalStateException("微信退款失败: " + e.getMessage());
        }
    }

    @Override
    public String parseNotify(byte[] body, Map<String, String> headers) {
        try {
            var root = objectMapper.readTree(body);
            return root.path("resource").path("ciphertext").isMissingNode()
                    ? root.path("out_trade_no").asText()
                    : decryptResource(root);
        } catch (Exception e) {
            throw new IllegalStateException("支付回调解析失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isMock() { return false; }

    /* ---------- V3 签名（商户私钥） ---------- */

    private String buildAuthHeader(String method, String path, Map<String, Object> body) throws Exception {
        long ts = System.currentTimeMillis() / 1000;
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String bodyStr = objectMapper.writeValueAsString(body);
        String message = method + "\n" + path + "\n" + ts + "\n" + nonce + "\n" + bodyStr + "\n";
        String signature = sign(message);
        return "WECHATPAY2-SHA256-RSA2048 mchid=\"" + props.getMchid()
                + "\",nonce_str=\"" + nonce + "\",timestamp=\"" + ts
                + "\",serial_no=\"" + props.getSerial() + "\",signature=\"" + signature + "\"";
    }

    private String sign(String message) {
        try {
            byte[] keyBytes = Files.readAllBytes(Path.of(props.getPrivateKeyPath()));
            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(
                            Base64.getDecoder().decode(new String(keyBytes)
                                    .replace("-----BEGIN PRIVATE KEY-----", "")
                                    .replace("-----END PRIVATE KEY-----", "")
                                    .replaceAll("\\s", ""))));
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new IllegalStateException("商户私钥签名失败: " + e.getMessage());
        }
    }

    private String decryptResource(com.fasterxml.jackson.databind.JsonNode root) throws Exception {
        // resource.ciphertext 用 apiV3Key AES-256-GCM 解密，明文 JSON 含 out_trade_no
        String nonce = root.path("resource").path("nonce").asText();
        String associated = root.path("resource").path("associated_data").asText();
        String cipherB64 = root.path("resource").path("ciphertext").asText();
        byte[] cipher = Base64.getDecoder().decode(cipherB64);
        javax.crypto.Cipher cipherInst = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
        var key = new javax.crypto.spec.SecretKeySpec(
                props.getApiV3Key().getBytes(java.nio.charset.StandardCharsets.UTF_8), "AES");
        cipherInst.init(javax.crypto.Cipher.DECRYPT_MODE, key,
                new javax.crypto.spec.GCMParameterSpec(128, nonce.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        if (!associated.isEmpty()) cipherInst.updateAAD(associated.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String plain = new String(cipherInst.doFinal(cipher), java.nio.charset.StandardCharsets.UTF_8);
        return objectMapper.readTree(plain).path("out_trade_no").asText();
    }
}
