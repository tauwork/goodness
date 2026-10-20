package com.abos.server.module.crm.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.crm.entity.CrmFollow;
import com.abos.server.module.crm.mapper.CrmFollowMapper;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CrmFollowService {

    private static final Set<String> TYPES = Set.of("VISIT", "MEETING", "CALL", "WECHAT", "OTHER");

    private final CrmFollowMapper followMapper;
    private final CustomerMapper customerMapper;
    private final CustomerAdminService customerAdmin;
    private final LeadMapper leadMapper;
    private final SysUserService staff;

    public IPage<CrmFollow> page(long page, long size, Long customerId, String followType, String keyword) {
        IPage<CrmFollow> result = followMapper.selectPage(new Page<>(page, size),
                Wrappers.<CrmFollow>lambdaQuery()
                        .eq(customerId != null, CrmFollow::getCustomerId, customerId)
                        .eq(StringUtils.hasText(followType), CrmFollow::getFollowType, followType)
                        .like(StringUtils.hasText(keyword), CrmFollow::getContent, keyword)
                        .orderByDesc(CrmFollow::getFollowAt));
        fill(result.getRecords());
        return result;
    }

    @Transactional
    public CrmFollow create(CrmFollow follow, Long ownerId) {
        if (!TYPES.contains(follow.getFollowType())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择跟进方式");
        }
        if (!StringUtils.hasText(follow.getContent())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写跟进内容");
        }
        Customer customer = customerAdmin.must(follow.getCustomerId());
        customerAdmin.ensureActive(customer);
        if (follow.getFollowAt() == null) {
            follow.setFollowAt(LocalDateTime.now());
        }
        follow.setId(null);
        follow.setOwnerId(ownerId);
        follow.setContent(follow.getContent().trim());
        followMapper.insert(follow);

        if (follow.getNextFollowAt() != null) {
            customer.setNextFollowAt(follow.getNextFollowAt());
            customerMapper.updateById(customer);
        }
        if (follow.getLeadId() != null) {
            Lead lead = leadMapper.selectById(follow.getLeadId());
            if (lead != null) {
                lead.setLastFollowAt(follow.getFollowAt());
                if ("ASSIGNED".equals(lead.getStatus()) || "NEW".equals(lead.getStatus())) {
                    lead.setStatus("IN_PROGRESS");
                }
                leadMapper.updateById(lead);
            }
        }
        return follow;
    }

    private void fill(List<CrmFollow> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> customerIds = rows.stream().map(CrmFollow::getCustomerId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Customer> customers = new java.util.HashMap<>();
        if (!customerIds.isEmpty()) {
            customerMapper.selectBatchIds(customerIds).forEach(c -> customers.put(c.getId(), c));
        }
        Map<Long, String> names = staff.names(rows.stream().map(CrmFollow::getOwnerId).filter(Objects::nonNull).toList());
        rows.forEach(row -> {
            Customer c = customers.get(row.getCustomerId());
            if (c != null) {
                row.setCustomerName(c.getName());
                row.setCustomerPhone(c.getPhone());
            }
            row.setOwnerName(names.get(row.getOwnerId()));
        });
    }
}
