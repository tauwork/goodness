package com.abos.server.module.customer.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.booking.entity.SvcBooking;
import com.abos.server.module.booking.mapper.SvcBookingMapper;
import com.abos.server.module.crm.entity.CrmFollow;
import com.abos.server.module.crm.mapper.CrmFollowMapper;
import com.abos.server.module.customer.dto.CustomerDetailVO;
import com.abos.server.module.customer.dto.CustomerProfileDTO;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.entity.FamilyMember;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.customer.mapper.FamilyMemberMapper;
import com.abos.server.module.lead.entity.Lead;
import com.abos.server.module.lead.mapper.LeadMapper;
import com.abos.server.module.sysuser.service.SysUserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerAdminService {

    private static final Set<String> STAGES = Set.of(
            Customer.STAGE_POOL, Customer.STAGE_INTENT, Customer.STAGE_SIGNED,
            Customer.STAGE_REPEAT, Customer.STAGE_BLACKLIST);
    private static final Set<String> OWNED = Set.of(
            Customer.STAGE_INTENT, Customer.STAGE_SIGNED, Customer.STAGE_REPEAT);
    private static final Set<String> RELATIONS = Set.of("SELF", "SPOUSE", "CHILD");

    private final CustomerMapper customerMapper;
    private final FamilyMemberMapper familyMapper;
    private final LeadMapper leadMapper;
    private final CrmFollowMapper followMapper;
    private final SvcBookingMapper bookingMapper;
    private final SysUserService staff;

    public IPage<Customer> page(long page, long size, String stage, String keyword) {
        IPage<Customer> result = customerMapper.selectPage(new Page<>(page, size),
                Wrappers.<Customer>lambdaQuery()
                        .eq(StringUtils.hasText(stage), Customer::getStage, stage)
                        .and(StringUtils.hasText(keyword), w -> w
                                .like(Customer::getName, keyword)
                                .or().like(Customer::getPhone, keyword)
                                .or().like(Customer::getWechat, keyword))
                        .orderByDesc(Customer::getUpdateTime));
        var names = staff.names(result.getRecords().stream().map(Customer::getOwnerId).filter(Objects::nonNull).toList());
        result.getRecords().forEach(c -> c.setOwnerName(names.get(c.getOwnerId())));
        return result;
    }

    public CustomerDetailVO detail(Long id) {
        Customer customer = must(id);
        customer.setOwnerName(staff.names(customer.getOwnerId() == null ? List.of() : List.of(customer.getOwnerId()))
                .get(customer.getOwnerId()));
        CustomerDetailVO vo = new CustomerDetailVO();
        vo.setCustomer(customer);
        vo.setFamily(familyMapper.selectList(Wrappers.<FamilyMember>lambdaQuery()
                .eq(FamilyMember::getCustomerId, id)
                .orderByAsc(FamilyMember::getCreateTime)));
        vo.setFollows(followMapper.selectList(Wrappers.<CrmFollow>lambdaQuery()
                .eq(CrmFollow::getCustomerId, id)
                .orderByDesc(CrmFollow::getFollowAt)
                .last("limit 20")));
        vo.setAppointments(bookingMapper.selectList(Wrappers.<SvcBooking>lambdaQuery()
                .eq(SvcBooking::getCustomerId, id)
                .orderByDesc(SvcBooking::getBookDate)
                .last("limit 20")));
        vo.setLeads(leadMapper.selectList(Wrappers.<Lead>lambdaQuery()
                .eq(Lead::getCustomerId, id)
                .orderByDesc(Lead::getCreateTime)
                .last("limit 20")));
        return vo;
    }

    public Customer findByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        return customerMapper.selectOne(Wrappers.<Customer>lambdaQuery().eq(Customer::getPhone, phone.trim()));
    }

    @Transactional
    public Customer create(CustomerProfileDTO dto, Long ownerId) {
        String phone = normalizePhone(dto.getPhone());
        if (findByPhone(phone) != null) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该手机号已有客户档案");
        }
        Customer c = new Customer();
        copyProfile(c, dto);
        c.setPhone(phone);
        c.setPhoneHash(hash(phone));
        c.setStage(Customer.STAGE_POOL);
        c.setSource(StringUtils.hasText(dto.getSource()) ? dto.getSource() : "STAFF");
        if (ownerId != null) {
            c.setOwnerId(ownerId);
            c.setStage(Customer.STAGE_INTENT);
        }
        try {
            customerMapper.insert(c);
        } catch (DuplicateKeyException e) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该手机号已有客户档案");
        }
        return c;
    }

    @Transactional
    public Customer update(Long id, CustomerProfileDTO dto) {
        Customer c = must(id);
        String phone = normalizePhone(dto.getPhone());
        Customer other = findByPhone(phone);
        if (other != null && !other.getId().equals(id)) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该手机号已有客户档案");
        }
        copyProfile(c, dto);
        c.setPhone(phone);
        c.setPhoneHash(hash(phone));
        try {
            customerMapper.updateById(c);
        } catch (DuplicateKeyException e) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该手机号已有客户档案");
        }
        return must(id);
    }

    /** 领取公海客户，进入意向并记到当前顾问名下 */
    public Customer claim(Long id, Long ownerId) {
        Customer c = must(id);
        if (Customer.STAGE_BLACKLIST.equals(c.getStage())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "黑名单客户不能领取");
        }
        if (!Customer.STAGE_POOL.equals(c.getStage())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "只有公海客户可以领取");
        }
        c.setStage(Customer.STAGE_INTENT);
        c.setOwnerId(ownerId);
        customerMapper.updateById(c);
        return c;
    }

    /** 退回公海，清空顾问 */
    public Customer release(Long id) {
        Customer c = must(id);
        if (Customer.STAGE_BLACKLIST.equals(c.getStage())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "黑名单客户请先移出黑名单");
        }
        customerMapper.update(null, Wrappers.<Customer>lambdaUpdate()
                .eq(Customer::getId, id)
                .set(Customer::getStage, Customer.STAGE_POOL)
                .set(Customer::getOwnerId, null));
        return must(id);
    }

    public Customer changeStage(Long id, String stage, Long ownerId) {
        if (!STAGES.contains(stage)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "客户阶段不正确");
        }
        Customer c = must(id);
        if (Customer.STAGE_POOL.equals(stage)) {
            return release(id);
        }
        if (Customer.STAGE_BLACKLIST.equals(stage)) {
            c.setStage(stage);
            customerMapper.updateById(c);
            return c;
        }
        if (OWNED.contains(stage)) {
            Long owner = c.getOwnerId() != null ? c.getOwnerId() : ownerId;
            if (owner == null) {
                throw new BizException(ErrorCode.BAD_REQUEST, "请先指定顾问");
            }
            c.setOwnerId(owner);
            c.setStage(stage);
            customerMapper.updateById(c);
            return c;
        }
        throw new BizException(ErrorCode.BAD_REQUEST, "客户阶段不正确");
    }

    public FamilyMember saveFamily(Long customerId, FamilyMember member) {
        must(customerId);
        if (!RELATIONS.contains(member.getRelation())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "关系请选本人、配偶或子女");
        }
        if (!StringUtils.hasText(member.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写家庭成员姓名");
        }
        member.setCustomerId(customerId);
        if (member.getId() == null) {
            familyMapper.insert(member);
            return member;
        }
        FamilyMember db = familyMapper.selectById(member.getId());
        if (db == null || !customerId.equals(db.getCustomerId())) {
            throw new BizException(ErrorCode.NOT_FOUND, "家庭成员不存在");
        }
        db.setRelation(member.getRelation());
        db.setName(member.getName());
        db.setBirthDate(member.getBirthDate());
        db.setSchoolStage(member.getSchoolStage());
        familyMapper.updateById(db);
        return db;
    }

    public void deleteFamily(Long customerId, Long memberId) {
        FamilyMember db = familyMapper.selectById(memberId);
        if (db == null || !customerId.equals(db.getCustomerId())) {
            throw new BizException(ErrorCode.NOT_FOUND, "家庭成员不存在");
        }
        familyMapper.deleteById(memberId);
    }

    public Customer must(Long id) {
        Customer c = customerMapper.selectById(id);
        if (c == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "客户不存在");
        }
        return c;
    }

    public void ensureActive(Customer c) {
        if (c != null && Customer.STAGE_BLACKLIST.equals(c.getStage())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该客户在黑名单中");
        }
    }

    private void copyProfile(Customer c, CustomerProfileDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写姓名");
        }
        c.setName(dto.getName().trim());
        c.setGender(blankToNull(dto.getGender()));
        c.setCity(blankToNull(dto.getCity()));
        c.setWechat(blankToNull(dto.getWechat()));
        c.setCompany(blankToNull(dto.getCompany()));
        c.setAddress(blankToNull(dto.getAddress()));
        c.setFamilyNote(blankToNull(dto.getFamilyNote()));
        c.setRemark(blankToNull(dto.getRemark()));
        if (StringUtils.hasText(dto.getSource())) {
            c.setSource(dto.getSource());
        }
    }

    private String normalizePhone(String phone) {
        String value = phone == null ? "" : phone.trim();
        if (!value.matches("^1[3-9]\\d{9}$")) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写正确的手机号");
        }
        return value;
    }

    public static String hash(String phone) {
        return DigestUtils.md5DigestAsHex(phone.getBytes(StandardCharsets.UTF_8));
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
