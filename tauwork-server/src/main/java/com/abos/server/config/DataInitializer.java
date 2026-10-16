package com.abos.server.config;

import com.abos.server.module.sysuser.entity.SysUser;
import com.abos.server.module.sysuser.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 初始管理员引导：sys_user 为空时创建 admin/admin123（bcrypt 现算）。
 * 上线前必须修改密码或删除该账号。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;

    @Override
    public void run(String[] args) {
        Long count = sysUserMapper.selectCount(Wrappers.<SysUser>lambdaQuery());
        if (count != null && count > 0) {
            return;
        }
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPasswordHash(new BCryptPasswordEncoder().encode("admin123"));
        admin.setRealName("系统管理员");
        admin.setRoleCode("ADMIN");
        admin.setStatus(1);
        sysUserMapper.insert(admin);
        log.warn("已创建初始管理员 admin/admin123，上线前必须修改密码！");
    }
}
