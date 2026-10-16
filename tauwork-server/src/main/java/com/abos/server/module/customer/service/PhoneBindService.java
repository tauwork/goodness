package com.abos.server.module.customer.service;

import cn.hutool.core.util.RandomUtil;
import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 手机号绑定（小程序客户）：
 *  开发模式：验证码只存内存并打日志（mock，不真实发短信）
 *  正式模式：接入短信通道后替换 sendSms()（配置 sms.enabled=true 时走真实发送）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneBindService {

    private final CustomerMapper customerMapper;

    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    /** code 缓存：手机号 → {code, expiresAt, tries}；单机内存即可（MVP 不引 Redis） */
    private final Map<String, CodeEntry> codeCache = new ConcurrentHashMap<>();

    private record CodeEntry(String code, long expiresAt) {}

    /** 发送验证码（60 秒内不可重发） */
    public void sendCode(String phone) {
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BizException(ErrorCode.BAD_REQUEST, "手机号格式不正确");
        }
        CodeEntry existing = codeCache.get(phone);
        if (existing != null && existing.expiresAt() - 5 * 60 * 1000 + 60 * 1000 > System.currentTimeMillis()) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "验证码已发送，60秒内不可重发");
        }
        String code = RandomUtil.randomNumbers(6);
        codeCache.put(phone, new CodeEntry(code, System.currentTimeMillis() + 5 * 60 * 1000));
        if (smsEnabled) {
            sendSms(phone, code);   // TODO 正式环境接短信通道
        } else {
            log.info("[MOCK短信] 手机号 {} 的验证码: {}", phone, code);
        }
    }

    /** 校验并绑定手机号（同手机号已有其他客户绑定时拒绝） */
    public void bind(Long customerId, String phone, String code) {
        CodeEntry entry = codeCache.get(phone);
        if (entry == null || entry.expiresAt() < System.currentTimeMillis()) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "验证码已过期，请重新获取");
        }
        if (!entry.code().equals(code)) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "验证码错误");
        }
        Long dup = customerMapper.selectCount(Wrappers.<Customer>lambdaQuery()
                .eq(Customer::getPhone, phone));
        if (dup != null && dup > 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该手机号已绑定其他账号，请联系客服");
        }
        Customer c = customerMapper.selectById(customerId);
        if (c == null) throw new BizException(ErrorCode.CUSTOMER_NOT_FOUND);
        c.setPhone(phone);
        c.setPhoneHash(org.springframework.util.DigestUtils.md5DigestAsHex(phone.getBytes()));
        customerMapper.updateById(c);
        codeCache.remove(phone);
        log.info("客户{}绑定手机号尾号{}", customerId, phone.substring(7));
    }

    private void sendSms(String phone, String code) {
        // 预留：阿里云/腾讯云短信 SDK
    }
}
