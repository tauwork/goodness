package com.abos.server.module.lead.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.customer.dto.CustomerProfileDTO;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.customer.service.CustomerAdminService;
import com.abos.server.module.lead.entity.Lead;
import com.abos.server.module.lead.mapper.LeadMapper;
import com.abos.server.module.sysuser.service.SysUserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadMapper leadMapper;
    private final CustomerMapper customerMapper;
    private final CustomerAdminService customerAdmin;
    private final SysUserService staff;

    public IPage<Lead> page(long page, long size, String status, String source, String keyword) {
        IPage<Lead> result = leadMapper.selectPage(new Page<>(page, size),
                Wrappers.<Lead>lambdaQuery()
                        .eq(StringUtils.hasText(status), Lead::getStatus, status)
                        .eq(StringUtils.hasText(source), Lead::getSource, source)
                        .and(StringUtils.hasText(keyword), w -> w
                                .like(Lead::getName, keyword)
                                .or().like(Lead::getPhone, keyword)
                                .or().like(Lead::getIntent, keyword)
                                .or().like(Lead::getRemark, keyword))
                        .orderByDesc(Lead::getCreateTime));
        fill(result.getRecords());
        return result;
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

    /** 按手机号归入客户库。没有档案时新建意向客户。 */
    @org.springframework.transaction.annotation.Transactional
    public Lead convert(Long id) {
        Lead lead = own(id);
        if ("CONVERTED".equals(lead.getStatus())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "这条线索已经转化");
        }
        Customer customer = null;
        if (lead.getCustomerId() != null) {
            customer = customerMapper.selectById(lead.getCustomerId());
        }
        if (customer == null && StringUtils.hasText(lead.getPhone())) {
            customer = customerAdmin.findByPhone(lead.getPhone());
        }
        if (customer == null) {
            if (!StringUtils.hasText(lead.getPhone())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "线索没有手机号，无法归入客户库");
            }
            CustomerProfileDTO profile = new CustomerProfileDTO();
            profile.setName(StringUtils.hasText(lead.getName()) ? lead.getName() : "未命名客户");
            profile.setPhone(lead.getPhone());
            profile.setSource(lead.getSource());
            customer = customerAdmin.create(profile, lead.getOwnerId());
        }
        customerAdmin.ensureActive(customer);
        if (Customer.STAGE_POOL.equals(customer.getStage()) && lead.getOwnerId() != null) {
            customer.setStage(Customer.STAGE_INTENT);
            customer.setOwnerId(lead.getOwnerId());
            customerMapper.updateById(customer);
        }
        lead.setCustomerId(customer.getId());
        if (!StringUtils.hasText(lead.getName())) {
            lead.setName(customer.getName());
        }
        if (!StringUtils.hasText(lead.getPhone())) {
            lead.setPhone(customer.getPhone());
        }
        lead.setStatus("CONVERTED");
        leadMapper.updateById(lead);
        return lead;
    }

    private void fill(List<Lead> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> customerIds = rows.stream().map(Lead::getCustomerId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Customer> customers = new HashMap<>();
        if (!customerIds.isEmpty()) {
            customerMapper.selectBatchIds(customerIds).forEach(c -> customers.put(c.getId(), c));
        }
        Map<Long, String> names = staff.names(rows.stream().map(Lead::getOwnerId).filter(Objects::nonNull).toList());
        rows.forEach(row -> {
            Customer c = row.getCustomerId() == null ? null : customers.get(row.getCustomerId());
            if (c != null) {
                if (!StringUtils.hasText(row.getName())) {
                    row.setName(c.getName());
                }
                if (!StringUtils.hasText(row.getPhone())) {
                    row.setPhone(c.getPhone());
                }
            }
            row.setOwnerName(names.get(row.getOwnerId()));
        });
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
