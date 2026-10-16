package com.abos.server.module.lead.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.lead.entity.Lead;
import com.abos.server.module.lead.mapper.LeadMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadMapper leadMapper;

    public IPage<Lead> page(long page, long size, String status, String source) {
        return leadMapper.selectPage(new Page<>(page, size),
                Wrappers.<Lead>lambdaQuery()
                        .eq(StringUtils.hasText(status), Lead::getStatus, status)
                        .eq(StringUtils.hasText(source), Lead::getSource, source)
                        .orderByDesc(Lead::getCreateTime));
    }

    /** 分配线索给顾问 */
    public Lead assign(Long id, Long ownerId) {
        Lead lead = leadMapper.selectById(id);
        if (lead == null) throw new BizException(ErrorCode.LEAD_NOT_FOUND);
        if ("ASSIGNED".equals(lead.getStatus()) || "IN_PROGRESS".equals(lead.getStatus())) {
            throw new BizException(ErrorCode.LEAD_ALREADY_ASSIGNED);
        }
        lead.setOwnerId(ownerId);
        lead.setStatus("ASSIGNED");
        lead.setAssignedAt(java.time.LocalDateTime.now());
        leadMapper.updateById(lead);
        return lead;
    }

    /** 从公海池领取线索（顾问自助） */
    public Lead claim(Long id, Long ownerId) {
        Lead lead = leadMapper.selectById(id);
        if (lead == null) throw new BizException(ErrorCode.LEAD_NOT_FOUND);
        if (!"PUBLIC_POOL".equals(lead.getStatus())) {
            throw new BizException(ErrorCode.LEAD_ALREADY_ASSIGNED, "仅公海池线索可领取");
        }
        lead.setOwnerId(ownerId);
        lead.setStatus("ASSIGNED");
        lead.setAssignedAt(java.time.LocalDateTime.now());
        lead.setLastFollowAt(java.time.LocalDateTime.now());
        leadMapper.updateById(lead);
        return lead;
    }

    /** 顾问跟进打卡：刷新 last_follow_at，状态 IN_PROGRESS；退回公海（放弃） */
    public Lead followUp(Long id) {
        Lead lead = own(id);
        lead.setLastFollowAt(java.time.LocalDateTime.now());
        if ("ASSIGNED".equals(lead.getStatus())) lead.setStatus("IN_PROGRESS");
        leadMapper.updateById(lead);
        return lead;
    }

    /** 放弃跟进 → 退回公海池 */
    public Lead release(Long id) {
        Lead lead = own(id);
        lead.setOwnerId(null);
        lead.setStatus("PUBLIC_POOL");
        lead.setAssignedAt(null);
        leadMapper.updateById(lead);
        return lead;
    }

    private Lead own(Long id) {
        Lead lead = leadMapper.selectById(id);
        if (lead == null) throw new BizException(ErrorCode.LEAD_NOT_FOUND);
        return lead;
    }

    /**
     * 公海池自动回收：每小时执行——已分配(ASSIGNED/IN_PROGRESS)且超过 72 小时未跟进的线索，
     * 收回公海池（owner 清空）。回收不覆盖 CONVERTED。
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 3_600_000, initialDelay = 60_000)
    public int recycleStale() {
        java.time.LocalDateTime deadline = java.time.LocalDateTime.now().minusHours(72);
        java.util.List<Lead> stale = leadMapper.selectList(Wrappers.<Lead>lambdaQuery()
                .in(Lead::getStatus, "ASSIGNED", "IN_PROGRESS")
                .isNotNull(Lead::getLastFollowAt)
                .lt(Lead::getLastFollowAt, deadline));
        // 从未打卡过的：以分配时间为准
        java.util.List<Lead> neverFollowed = leadMapper.selectList(Wrappers.<Lead>lambdaQuery()
                .in(Lead::getStatus, "ASSIGNED")
                .isNull(Lead::getLastFollowAt)
                .isNotNull(Lead::getAssignedAt)
                .lt(Lead::getAssignedAt, deadline));
        java.util.List<Lead> all = new java.util.ArrayList<>(stale);
        all.addAll(neverFollowed);
        all.forEach(l -> {
            l.setOwnerId(null);
            l.setStatus("PUBLIC_POOL");
            leadMapper.updateById(l);
        });
        if (!all.isEmpty()) {
            log.warn("公海池自动回收 {} 条超72小时未跟进线索", all.size());
        }
        return all.size();
    }
}
