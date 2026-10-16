package com.abos.server.module.lead.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.lead.entity.Lead;
import com.abos.server.module.lead.mapper.LeadMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
}
