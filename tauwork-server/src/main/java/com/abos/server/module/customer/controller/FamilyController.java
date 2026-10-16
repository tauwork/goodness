package com.abos.server.module.customer.controller;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.common.result.Result;
import com.abos.server.module.customer.entity.FamilyMember;
import com.abos.server.module.customer.mapper.FamilyMemberMapper;
import com.abos.server.security.LoginContext;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 小程序家庭档案（需登录）：多孩管理
 *  GET/POST/PUT/DELETE /api/mini/family
 */
@RestController
@RequestMapping("/api/mini/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyMemberMapper familyMapper;

    private static final Set<String> RELATIONS = Set.of("SELF", "SPOUSE", "CHILD");

    @GetMapping("/list")
    public Result<List<FamilyMember>> list() {
        return Result.ok(familyMapper.selectList(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getCustomerId, LoginContext.currentCustomerId())
                .orderByAsc(FamilyMember::getCreateTime)));
    }

    @PostMapping
    public Result<FamilyMember> create(@RequestBody FamilyMember m) {
        if (!RELATIONS.contains(m.getRelation())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "关系须为 SELF/SPOUSE/CHILD");
        }
        m.setId(null);
        m.setCustomerId(LoginContext.currentCustomerId());
        familyMapper.insert(m);
        return Result.ok(m);
    }

    @PutMapping
    public Result<FamilyMember> update(@RequestBody FamilyMember m) {
        FamilyMember db = own(m.getId());
        db.setRelation(m.getRelation());
        db.setName(m.getName());
        db.setBirthDate(m.getBirthDate());
        db.setSchoolStage(m.getSchoolStage());
        familyMapper.updateById(db);
        return Result.ok(db);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        familyMapper.deleteById(own(id).getId());
        return Result.ok();
    }

    private FamilyMember own(Long id) {
        FamilyMember m = familyMapper.selectById(id);
        if (m == null || !m.getCustomerId().equals(LoginContext.currentCustomerId())) {
            throw new BizException(ErrorCode.NOT_FOUND, "家庭成员不存在");
        }
        return m;
    }
}
