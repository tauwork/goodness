package com.abos.server.module.team.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.community.entity.Community;
import com.abos.server.module.community.mapper.CommunityMapper;
import com.abos.server.module.team.entity.Consultant;
import com.abos.server.module.team.mapper.ConsultantMapper;
import com.abos.server.security.AccessScope;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsultantService {

    private final ConsultantMapper consultantMapper;
    private final CommunityMapper communityMapper;
    private final AccessScope access;

    public List<Consultant> list(Long communityId) {
        AccessScope.View scope = access.current();
        if (!scope.all() && scope.communityIds().isEmpty()) {
            return List.of();
        }
        if (communityId != null) {
            ensureCommunity(communityId);
        }
        List<Consultant> rows = consultantMapper.selectList(Wrappers.<Consultant>lambdaQuery()
                .eq(communityId != null, Consultant::getCommunityId, communityId)
                .in(!scope.all(), Consultant::getCommunityId, scope.communityIds())
                .orderByAsc(Consultant::getCommunityId)
                .orderByAsc(Consultant::getName));
        fillCommunity(rows);
        return rows;
    }

    @Transactional
    public Consultant create(Consultant dto) {
        Consultant row = new Consultant();
        copy(row, dto);
        consultantMapper.insert(row);
        fillCommunity(List.of(row));
        return row;
    }

    @Transactional
    public Consultant update(Consultant dto) {
        Consultant row = must(dto.getId());
        ensureCommunity(row.getCommunityId());
        copy(row, dto);
        consultantMapper.updateById(row);
        fillCommunity(List.of(row));
        return row;
    }

    @Transactional
    public void delete(Long id) {
        Consultant row = must(id);
        ensureCommunity(row.getCommunityId());
        consultantMapper.deleteById(id);
    }

    private Consultant must(Long id) {
        Consultant row = id == null ? null : consultantMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "顾问不存在");
        }
        return row;
    }

    private void copy(Consultant row, Consultant dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写姓名");
        }
        ensureCommunity(dto.getCommunityId());
        row.setName(dto.getName().trim());
        row.setAvatarUrl(trim(dto.getAvatarUrl()));
        row.setTitle(trim(dto.getTitle()));
        row.setValueService(trim(dto.getValueService()));
        row.setPersonality(trim(dto.getPersonality()));
        row.setWechat(trim(dto.getWechat()));
        row.setPhone(trim(dto.getPhone()));
        row.setCommunityId(dto.getCommunityId());
    }

    private void ensureCommunity(Long communityId) {
        if (communityId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择归属社区");
        }
        Community community = communityMapper.selectById(communityId);
        if (community == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "社区不存在");
        }
        AccessScope.View scope = access.current();
        if (!scope.all() && !scope.communityIds().contains(communityId)) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "无权维护这个社区的顾问");
        }
    }

    private void fillCommunity(List<Consultant> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> ids = rows.stream().map(Consultant::getCommunityId).distinct().toList();
        Map<Long, String> names = new HashMap<>();
        communityMapper.selectBatchIds(ids).forEach(community -> names.put(community.getId(), community.getName()));
        rows.forEach(row -> row.setCommunityName(names.get(row.getCommunityId())));
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
