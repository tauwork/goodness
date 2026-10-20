package com.abos.server.module.customer.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 小程序微信登录：
 *  正式模式：wx.login code → code2session 换 openid（需小程序 appid/secret 配置）
 *  开发模式：未配置 appid 时使用 mock openid（mock-{code}），保证前后端联调不依赖真实小程序
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxAuthService {

    private final CustomerMapper customerMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${wx.mini.appid:}")
    private String appid;

    @Value("${wx.mini.secret:}")
    private String secret;

    public record LoginVO(String token, Long customerId, boolean mock) {}

    public LoginVO loginByCode(String code) {
        String openid;
        boolean mock;
        if (!StringUtils.hasText(appid) || appid.startsWith("mock")) {
            // 开发模式：mock openid，同一 code 复用同一身份
            openid = "mock-" + code;
            mock = true;
        } else {
            openid = code2session(code);
            mock = false;
        }

        Customer customer = customerMapper.selectOne(Wrappers.<Customer>lambdaQuery()
                .eq(Customer::getWxOpenid, openid));
        if (customer == null) {
            customer = new Customer();
            customer.setWxOpenid(openid);
            customer.setStage(Customer.STAGE_POOL);
            customer.setSource("OTHER");
            customerMapper.insert(customer);
            log.info("新小程序客户注册: openid={}, mock={}", openid, mock);
        }

        StpUtil.login("C" + customer.getId());   // C 前缀区分后台账号ID命名空间
        return new LoginVO(StpUtil.getTokenValue(), customer.getId(), mock);
    }

    private String code2session(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid
                + "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";
        // 微信该接口的 Content-Type 是 text/plain，不能按 JSON 媒体类型直接反序列化
        ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
        String raw = entity.getBody();
        if (!StringUtils.hasText(raw)) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "微信登录失败: 空响应");
        }
        Map<String, Object> resp;
        try {
            resp = objectMapper.readValue(raw, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("code2session 响应无法解析");
            throw new BizException(ErrorCode.OPERATION_FAILED, "微信登录失败");
        }
        Object err = resp.get("errcode");
        if (err != null && !"0".equals(String.valueOf(err))) {
            log.error("code2session 失败: errcode={}, errmsg={}", err, resp.get("errmsg"));
            throw new BizException(ErrorCode.OPERATION_FAILED, "微信登录失败: " + resp.get("errmsg"));
        }
        Object openid = resp.get("openid");
        if (openid == null || !StringUtils.hasText(openid.toString())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "微信登录失败: 未返回身份");
        }
        return openid.toString();
    }
}
