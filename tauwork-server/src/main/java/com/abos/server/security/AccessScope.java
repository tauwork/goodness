package com.abos.server.security;

import com.abos.server.module.sysuser.entity.SysUser;
import com.abos.server.module.sysuser.mapper.SysUserMapper;
import com.abos.server.module.sysuser.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 当前后台账号能看到哪些社区。系统管理员不限；其他账号只看归属社区。
 */
@Component
@RequiredArgsConstructor
public class AccessScope {

    private final SysUserMapper userMapper;
    private final RbacService rbac;

    public View current() {
        Long id;
        try {
            id = LoginContext.currentAdminId();
        } catch (Exception ex) {
            return new View(false, List.of(), List.of("VIDEO"));
        }
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            return new View(false, List.of(), List.of("VIDEO"));
        }
        if (rbac.seesAllCommunities(user)) {
            return new View(true, List.of(), List.of());
        }
        List<Long> communities = rbac.communityIdsOf(user.getId());
        return new View(false, communities, rbac.bookingModes(communities));
    }

    public record View(boolean all, List<Long> communityIds, List<String> bookingModes) {
    }
}
