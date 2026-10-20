package com.abos.server.module.partner.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.customer.dto.CustomerProfileDTO;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.customer.service.CustomerAdminService;
import com.abos.server.module.order.entity.Order;
import com.abos.server.module.order.mapper.OrderMapper;
import com.abos.server.module.partner.dto.PartnerCustomerVO;
import com.abos.server.module.partner.dto.PartnerDetailVO;
import com.abos.server.module.partner.dto.PartnerHomeVO;
import com.abos.server.module.partner.dto.PartnerSaveDTO;
import com.abos.server.module.partner.entity.Partner;
import com.abos.server.module.partner.entity.PartnerCommission;
import com.abos.server.module.partner.entity.PartnerRule;
import com.abos.server.module.partner.entity.PartnerWithdraw;
import com.abos.server.module.partner.mapper.PartnerCommissionMapper;
import com.abos.server.module.partner.mapper.PartnerMapper;
import com.abos.server.module.partner.mapper.PartnerRuleMapper;
import com.abos.server.module.partner.mapper.PartnerWithdrawMapper;
import com.abos.server.module.product.entity.ProductCategory;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.product.mapper.ProductCategoryMapper;
import com.abos.server.module.product.mapper.SvcProductMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PartnerService {

    public static final String BIND_LINK = "LINK";
    public static final String BIND_ADMIN = "ADMIN";

    private final PartnerMapper partnerMapper;
    private final PartnerRuleMapper ruleMapper;
    private final PartnerCommissionMapper commissionMapper;
    private final PartnerWithdrawMapper withdrawMapper;
    private final CustomerMapper customerMapper;
    private final CustomerAdminService customerAdmin;
    private final ProductCategoryMapper categoryMapper;
    private final SvcProductMapper productMapper;
    private final OrderMapper orderMapper;

    public List<Partner> list() {
        List<Partner> rows = partnerMapper.selectList(Wrappers.<Partner>lambdaQuery().orderByDesc(Partner::getCreateTime));
        fillIdentity(rows);
        fillStats(rows);
        return rows;
    }

    public List<Partner> options() {
        List<Partner> rows = partnerMapper.selectList(Wrappers.<Partner>lambdaQuery()
                .eq(Partner::getStatus, 1)
                .orderByDesc(Partner::getCreateTime));
        fillIdentity(rows);
        return rows;
    }

    public PartnerDetailVO detail(Long id) {
        Partner partner = must(id);
        fillIdentity(List.of(partner));
        List<PartnerCommission> deals = commissionMapper.selectList(Wrappers.<PartnerCommission>lambdaQuery()
                .eq(PartnerCommission::getPartnerId, id)
                .orderByDesc(PartnerCommission::getCreateTime));
        fillDealCustomers(deals);
        fillStats(List.of(partner));
        PartnerDetailVO vo = new PartnerDetailVO();
        vo.setPartner(partner);
        vo.setDeals(deals);
        vo.setCustomers(customersOf(id));
        vo.setWithdraws(withdrawMapper.selectList(Wrappers.<PartnerWithdraw>lambdaQuery()
                .eq(PartnerWithdraw::getPartnerId, id)
                .orderByDesc(PartnerWithdraw::getCreateTime)));
        return vo;
    }

    @Transactional
    public Partner create(PartnerSaveDTO dto) {
        if (!StringUtils.hasText(dto.getName()) || !StringUtils.hasText(dto.getPhone())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写姓名和手机号");
        }
        Customer customer = customerAdmin.findByPhone(dto.getPhone().trim());
        if (customer == null) {
            CustomerProfileDTO profile = new CustomerProfileDTO();
            profile.setName(dto.getName().trim());
            profile.setPhone(dto.getPhone().trim());
            profile.setSource("REFERRAL");
            customer = customerAdmin.create(profile, null);
        } else if (!StringUtils.hasText(customer.getName())) {
            customer.setName(dto.getName().trim());
            customerMapper.updateById(customer);
        }
        customerAdmin.ensureActive(customer);
        Partner existing = partnerMapper.selectOne(Wrappers.<Partner>lambdaQuery()
                .eq(Partner::getCustomerId, customer.getId()));
        if (existing != null) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "这个客户已经是合伙人");
        }
        Partner partner = new Partner();
        partner.setCustomerId(customer.getId());
        partner.setCode(newCode());
        partner.setParentId(validParent(null, dto.getParentId()));
        partner.setStatus(1);
        partnerMapper.insert(partner);
        partner.setName(customer.getName());
        partner.setPhone(customer.getPhone());
        partner.setCommissionAmount(BigDecimal.ZERO);
        partner.setDealCount(0L);
        return partner;
    }

    @Transactional
    public Partner changeStatus(Long id, int status) {
        Partner partner = must(id);
        partner.setStatus(status == 1 ? 1 : 0);
        partnerMapper.updateById(partner);
        return partner;
    }

    /** 后台指定或清空客户归属。已有归属会被这次指定覆盖。 */
    @Transactional
    public void assign(Long customerId, Long partnerId) {
        Customer customer = customerAdmin.must(customerId);
        if (partnerId == null) {
            customerMapper.update(null, Wrappers.<Customer>lambdaUpdate()
                    .set(Customer::getPartnerId, null)
                    .set(Customer::getPartnerBindAt, null)
                    .set(Customer::getPartnerBindSource, null)
                    .eq(Customer::getId, customerId));
            return;
        }
        Partner partner = must(partnerId);
        if (partner.getStatus() == null || partner.getStatus() != 1) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "这个合伙人已停用");
        }
        if (partner.getCustomerId().equals(customer.getId())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "合伙人不能归属给自己");
        }
        customerAdmin.ensureActive(customer);
        customer.setPartnerId(partner.getId());
        customer.setPartnerBindAt(LocalDateTime.now());
        customer.setPartnerBindSource(BIND_ADMIN);
        customerMapper.updateById(customer);
    }

    @Transactional
    public void assignByPhone(Long partnerId, String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写客户手机号");
        }
        Customer customer = customerAdmin.findByPhone(phone.trim());
        if (customer == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "这个手机号还没有客户档案");
        }
        assign(customer.getId(), partnerId);
    }

    /** 分享链接第一次打开时绑定。已有归属不覆盖。 */
    @Transactional
    public void bindByCode(Long customerId, String code) {
        if (!StringUtils.hasText(code)) {
            return;
        }
        Partner partner = partnerMapper.selectOne(Wrappers.<Partner>lambdaQuery()
                .eq(Partner::getCode, code.trim().toUpperCase())
                .eq(Partner::getStatus, 1));
        if (partner == null || partner.getCustomerId().equals(customerId)) {
            return;
        }
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null || customer.getPartnerId() != null || Customer.STAGE_BLACKLIST.equals(customer.getStage())) {
            return;
        }
        customer.setPartnerId(partner.getId());
        customer.setPartnerBindAt(LocalDateTime.now());
        customer.setPartnerBindSource(BIND_LINK);
        customerMapper.updateById(customer);
    }

    public PartnerHomeVO home(Long customerId) {
        Partner partner = partnerMapper.selectOne(Wrappers.<Partner>lambdaQuery()
                .eq(Partner::getCustomerId, customerId)
                .eq(Partner::getStatus, 1));
        PartnerHomeVO vo = new PartnerHomeVO();
        if (partner == null) {
            vo.setPartner(false);
            vo.setCommissionAmount(BigDecimal.ZERO);
            vo.setDirectAmount(BigDecimal.ZERO);
            vo.setIndirectAmount(BigDecimal.ZERO);
            vo.setOtherAmount(BigDecimal.ZERO);
            vo.setWithdrawnAmount(BigDecimal.ZERO);
            vo.setPendingAmount(BigDecimal.ZERO);
            vo.setBalanceAmount(BigDecimal.ZERO);
            vo.setCustomerCount(0);
            vo.setDirectCount(0);
            vo.setIndirectCount(0);
            vo.setCustomers(List.of());
            vo.setDeals(List.of());
            return vo;
        }
        fillIdentity(List.of(partner));
        fillStats(List.of(partner));
        List<PartnerCommission> deals = commissionMapper.selectList(Wrappers.<PartnerCommission>lambdaQuery()
                .eq(PartnerCommission::getPartnerId, partner.getId())
                .eq(PartnerCommission::getStatus, PartnerCommission.ACCRUED)
                .orderByDesc(PartnerCommission::getCreateTime));
        fillDealCustomers(deals);
        vo.setPartner(true);
        vo.setName(partner.getName());
        vo.setPhone(partner.getPhone());
        vo.setCode(partner.getCode());
        vo.setSharePath("/pages/index/index?partner=" + partner.getCode());
        vo.setJoinPath("/pages/me/partner-join?partner=" + partner.getCode());
        vo.setDeals(deals);
        vo.setCustomers(customersOf(partner.getId()));
        vo.setCustomerCount((partner.getDirectCount() == null ? 0 : partner.getDirectCount())
                + (partner.getIndirectCount() == null ? 0 : partner.getIndirectCount()));
        vo.setDirectCount(partner.getDirectCount() == null ? 0 : partner.getDirectCount());
        vo.setIndirectCount(partner.getIndirectCount() == null ? 0 : partner.getIndirectCount());
        vo.setCommissionAmount(nz(partner.getCommissionAmount()));
        vo.setDirectAmount(nz(partner.getDirectAmount()));
        vo.setIndirectAmount(nz(partner.getIndirectAmount()));
        vo.setOtherAmount(nz(partner.getOtherAmount()));
        vo.setWithdrawnAmount(nz(partner.getWithdrawnAmount()));
        vo.setPendingAmount(pendingAmount(partner.getId()));
        vo.setBalanceAmount(nz(partner.getBalanceAmount()));
        return vo;
    }

    /** 海报邀请：成为上级合伙人的下级。已是合伙人不重复开通。 */
    @Transactional
    public Partner join(Long customerId, String code) {
        if (!StringUtils.hasText(code)) {
            throw new BizException(ErrorCode.BAD_REQUEST, "缺少邀请码");
        }
        Partner parent = partnerMapper.selectOne(Wrappers.<Partner>lambdaQuery()
                .eq(Partner::getCode, code.trim().toUpperCase())
                .eq(Partner::getStatus, 1));
        if (parent == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "邀请已失效");
        }
        Customer customer = customerAdmin.must(customerId);
        if (!StringUtils.hasText(customer.getPhone())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请先绑定手机号");
        }
        if (!StringUtils.hasText(customer.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请先在档案里填写姓名");
        }
        customerAdmin.ensureActive(customer);
        Partner existing = partnerMapper.selectOne(Wrappers.<Partner>lambdaQuery()
                .eq(Partner::getCustomerId, customer.getId()));
        if (existing != null) {
            return existing;
        }
        if (parent.getCustomerId().equals(customer.getId())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "不能使用自己的邀请");
        }
        Partner partner = new Partner();
        partner.setCustomerId(customer.getId());
        partner.setCode(newCode());
        partner.setParentId(parent.getId());
        partner.setStatus(1);
        partnerMapper.insert(partner);
        return partner;
    }

    @Transactional
    public PartnerWithdraw requestWithdraw(Long customerId, BigDecimal amount) {
        Partner partner = partnerMapper.selectOne(Wrappers.<Partner>lambdaQuery()
                .eq(Partner::getCustomerId, customerId)
                .eq(Partner::getStatus, 1));
        if (partner == null) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "还不是合伙人");
        }
        return createWithdraw(partner.getId(), amount, "申请提现", PartnerWithdraw.PENDING);
    }

    @Transactional
    public PartnerWithdraw recordWithdraw(Long partnerId, BigDecimal amount, String remark) {
        must(partnerId);
        return createWithdraw(partnerId, amount, remark, PartnerWithdraw.PAID);
    }

    @Transactional
    public void reviewWithdraw(Long id, String status) {
        PartnerWithdraw row = withdrawMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "提现记录不存在");
        }
        if (!PartnerWithdraw.PENDING.equals(row.getStatus())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "这笔提现已经处理过");
        }
        if (PartnerWithdraw.PAID.equals(status)) {
            row.setStatus(PartnerWithdraw.PAID);
        } else if (PartnerWithdraw.REJECTED.equals(status)) {
            row.setStatus(PartnerWithdraw.REJECTED);
        } else {
            throw new BizException(ErrorCode.BAD_REQUEST, "只能通过或驳回");
        }
        withdrawMapper.updateById(row);
    }

    @Transactional
    public PartnerCommission recordOther(Long partnerId, BigDecimal amount, String remark) {
        Partner partner = must(partnerId);
        if (amount == null || amount.signum() <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "金额要大于 0");
        }
        PartnerCommission row = new PartnerCommission();
        row.setPartnerId(partner.getId());
        row.setCustomerId(partner.getCustomerId());
        row.setProductName(StringUtils.hasText(remark) ? remark.trim() : "其它佣金");
        row.setOrderAmount(amount);
        row.setRate(BigDecimal.valueOf(100));
        row.setCommissionAmount(amount.setScale(2, RoundingMode.HALF_UP));
        row.setStatus(PartnerCommission.ACCRUED);
        row.setSource(PartnerCommission.SOURCE_OTHER);
        row.setLevel(PartnerCommission.LEVEL_OTHER);
        commissionMapper.insert(row);
        return row;
    }

    public List<PartnerRule> rules() {
        Map<Long, PartnerRule> byCategory = new HashMap<>();
        ruleMapper.selectList(Wrappers.<PartnerRule>lambdaQuery()).forEach(rule -> byCategory.put(rule.getCategoryId(), rule));
        return categoryMapper.selectList(Wrappers.<ProductCategory>lambdaQuery()
                        .eq(ProductCategory::getStatus, 1)
                        .orderByAsc(ProductCategory::getProdType)
                        .orderByAsc(ProductCategory::getSortNo))
                .stream().map(category -> {
                    PartnerRule rule = byCategory.get(category.getId());
                    if (rule == null) {
                        rule = new PartnerRule();
                        rule.setCategoryId(category.getId());
                        rule.setRate(BigDecimal.ZERO);
                        rule.setRateL2(BigDecimal.ZERO);
                    }
                    rule.setCategoryName(category.getName());
                    rule.setProdType(category.getProdType());
                    return rule;
                }).toList();
    }

    @Transactional
    public PartnerRule saveRule(Long categoryId, BigDecimal rate, BigDecimal rateL2) {
        if (categoryId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择类目");
        }
        if (categoryMapper.selectById(categoryId) == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "类目不存在");
        }
        BigDecimal direct = normalizeRate(rate);
        BigDecimal indirect = normalizeRate(rateL2);
        PartnerRule rule = ruleMapper.selectOne(Wrappers.<PartnerRule>lambdaQuery()
                .eq(PartnerRule::getCategoryId, categoryId));
        if (rule == null) {
            rule = new PartnerRule();
            rule.setCategoryId(categoryId);
            rule.setRate(direct);
            rule.setRateL2(indirect);
            ruleMapper.insert(rule);
        } else {
            rule.setRate(direct);
            rule.setRateL2(indirect);
            ruleMapper.updateById(rule);
        }
        return rule;
    }

    /** 服务项目没有在线支付，由后台按类目比例登记成交。 */
    @Transactional
    public PartnerCommission recordDeal(Long partnerId, PartnerSaveDTO dto) {
        Partner partner = must(partnerId);
        if (partner.getStatus() == null || partner.getStatus() != 1) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "这个合伙人已停用");
        }
        if (dto.getCustomerId() == null || dto.getProductId() == null || dto.getAmount() == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请选择客户、项目和成交金额");
        }
        if (dto.getAmount().signum() <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "成交金额要大于 0");
        }
        Customer customer = customerAdmin.must(dto.getCustomerId());
        if (!partner.getId().equals(customer.getPartnerId())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "这个客户还没归属该合伙人");
        }
        SvcProduct product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND, "项目不存在");
        }
        return insertCommission(partner, customer, null, null, product, dto.getAmount(), PartnerCommission.SOURCE_MANUAL);
    }

    @Transactional
    public void reverseDeal(Long commissionId) {
        PartnerCommission row = commissionMapper.selectById(commissionId);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "提成记录不存在");
        }
        if (!PartnerCommission.ACCRUED.equals(row.getStatus())) {
            return;
        }
        reverseOne(row);
        commissionMapper.selectList(Wrappers.<PartnerCommission>lambdaQuery()
                        .eq(PartnerCommission::getRefCommissionId, row.getId())
                        .eq(PartnerCommission::getStatus, PartnerCommission.ACCRUED))
                .forEach(this::reverseOne);
        if (row.getRefCommissionId() != null) {
            PartnerCommission origin = commissionMapper.selectById(row.getRefCommissionId());
            if (origin != null && PartnerCommission.ACCRUED.equals(origin.getStatus())) {
                reverseOne(origin);
            }
        }
    }

    private void reverseOne(PartnerCommission row) {
        row.setStatus(PartnerCommission.REVERSED);
        commissionMapper.updateById(row);
    }

    /** 支付成功后按客户归属和类目比例记一笔。没有归属或比例为 0 时不记。 */
    @Transactional
    public void accrue(Order order) {
        if (order == null || order.getId() == null || !"PAID".equals(order.getPayStatus())) {
            return;
        }
        Long existing = commissionMapper.selectCount(Wrappers.<PartnerCommission>lambdaQuery()
                .eq(PartnerCommission::getOrderId, order.getId())
                .eq(PartnerCommission::getStatus, PartnerCommission.ACCRUED));
        if (existing != null && existing > 0) {
            return;
        }
        Customer customer = order.getCustomerId() == null ? null : customerMapper.selectById(order.getCustomerId());
        if (customer == null || customer.getPartnerId() == null || Customer.STAGE_BLACKLIST.equals(customer.getStage())) {
            return;
        }
        Partner partner = partnerMapper.selectById(customer.getPartnerId());
        if (partner == null || partner.getStatus() == null || partner.getStatus() != 1
                || partner.getCustomerId().equals(customer.getId())) {
            return;
        }
        SvcProduct product = order.getProductId() == null ? null : productMapper.selectById(order.getProductId());
        if (product == null) {
            return;
        }
        insertCommission(partner, customer, order.getId(), order.getOrderNo(), product, order.getAmount(), PartnerCommission.SOURCE_ORDER);
        order.setDistRefId(partner.getId());
        orderMapper.updateById(order);
    }

    @Transactional
    public void reverseOrder(Long orderId) {
        if (orderId == null) {
            return;
        }
        commissionMapper.selectList(Wrappers.<PartnerCommission>lambdaQuery()
                        .eq(PartnerCommission::getOrderId, orderId)
                        .eq(PartnerCommission::getStatus, PartnerCommission.ACCRUED))
                .forEach(row -> {
                    row.setStatus(PartnerCommission.REVERSED);
                    commissionMapper.updateById(row);
                });
    }

    private PartnerCommission insertCommission(Partner partner, Customer customer, Long orderId, String orderNo,
                                              SvcProduct product, BigDecimal amount, String source) {
        if (product.getCategoryId() == null) {
            if (PartnerCommission.SOURCE_MANUAL.equals(source)) {
                throw new BizException(ErrorCode.OPERATION_FAILED, "这个项目没有类目，无法计算提成");
            }
            return null;
        }
        PartnerRule rule = ruleMapper.selectOne(Wrappers.<PartnerRule>lambdaQuery()
                .eq(PartnerRule::getCategoryId, product.getCategoryId()));
        if (rule == null || rule.getRate() == null || rule.getRate().signum() <= 0) {
            if (PartnerCommission.SOURCE_MANUAL.equals(source)) {
                throw new BizException(ErrorCode.OPERATION_FAILED, "这个类目的提成比例还是 0");
            }
            return null;
        }
        ProductCategory category = categoryMapper.selectById(product.getCategoryId());
        PartnerCommission row = new PartnerCommission();
        row.setPartnerId(partner.getId());
        row.setCustomerId(customer.getId());
        row.setOrderId(orderId);
        row.setOrderNo(orderNo);
        row.setProductId(product.getId());
        row.setProductName(product.getName());
        row.setCategoryId(product.getCategoryId());
        row.setCategoryName(category == null ? null : category.getName());
        row.setOrderAmount(amount);
        row.setRate(rule.getRate());
        row.setCommissionAmount(amount.multiply(rule.getRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        row.setStatus(PartnerCommission.ACCRUED);
        row.setSource(source);
        row.setLevel(PartnerCommission.LEVEL_DIRECT);
        commissionMapper.insert(row);
        accrueIndirect(partner, customer, row, rule, category, amount, source);
        return row;
    }

    private void accrueIndirect(Partner partner, Customer customer, PartnerCommission direct, PartnerRule rule,
                                ProductCategory category, BigDecimal amount, String source) {
        if (partner.getParentId() == null || rule.getRateL2() == null || rule.getRateL2().signum() <= 0) {
            return;
        }
        Partner parent = partnerMapper.selectById(partner.getParentId());
        if (parent == null || parent.getStatus() == null || parent.getStatus() != 1
                || parent.getCustomerId().equals(customer.getId())) {
            return;
        }
        PartnerCommission row = new PartnerCommission();
        row.setPartnerId(parent.getId());
        row.setCustomerId(customer.getId());
        row.setOrderId(direct.getOrderId());
        row.setOrderNo(direct.getOrderNo());
        row.setProductId(direct.getProductId());
        row.setProductName(direct.getProductName());
        row.setCategoryId(direct.getCategoryId());
        row.setCategoryName(category == null ? direct.getCategoryName() : category.getName());
        row.setOrderAmount(amount);
        row.setRate(rule.getRateL2());
        row.setCommissionAmount(amount.multiply(rule.getRateL2()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        row.setStatus(PartnerCommission.ACCRUED);
        row.setSource(source);
        row.setLevel(PartnerCommission.LEVEL_INDIRECT);
        row.setRefCommissionId(direct.getId());
        commissionMapper.insert(row);
    }

    private List<PartnerCustomerVO> customersOf(Long partnerId) {
        List<Partner> children = partnerMapper.selectList(Wrappers.<Partner>lambdaQuery()
                .eq(Partner::getParentId, partnerId));
        Map<Long, String> relationOfPartner = new HashMap<>();
        relationOfPartner.put(partnerId, "DIRECT");
        children.forEach(child -> relationOfPartner.put(child.getId(), "INDIRECT"));
        if (relationOfPartner.isEmpty()) {
            return List.of();
        }
        List<Customer> rows = customerMapper.selectList(Wrappers.<Customer>lambdaQuery()
                .in(Customer::getPartnerId, relationOfPartner.keySet())
                .orderByDesc(Customer::getPartnerBindAt));
        Partner self = partnerMapper.selectById(partnerId);
        List<PartnerCustomerVO> result = new ArrayList<>();
        for (Customer customer : rows) {
            Partner owner = children.stream().filter(child -> child.getId().equals(customer.getPartnerId())).findFirst().orElse(null);
            if (owner != null && owner.getCustomerId().equals(customer.getId())) {
                continue;
            }
            if (self != null && self.getCustomerId().equals(customer.getId())) {
                continue;
            }
            scrub(customer);
            PartnerCustomerVO vo = new PartnerCustomerVO();
            vo.setId(customer.getId());
            vo.setName(customer.getName());
            vo.setPhone(customer.getPhone());
            vo.setPartnerBindSource(customer.getPartnerBindSource());
            vo.setPartnerBindAt(customer.getPartnerBindAt());
            vo.setRelation(relationOfPartner.get(customer.getPartnerId()));
            result.add(vo);
        }
        return result;
    }

    private void fillStats(List<Partner> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> ids = rows.stream().map(Partner::getId).toList();
        Map<Long, BigDecimal> direct = new HashMap<>();
        Map<Long, BigDecimal> indirect = new HashMap<>();
        Map<Long, BigDecimal> other = new HashMap<>();
        Map<Long, Long> deals = new HashMap<>();
        commissionMapper.selectList(Wrappers.<PartnerCommission>lambdaQuery()
                        .in(PartnerCommission::getPartnerId, ids)
                        .eq(PartnerCommission::getStatus, PartnerCommission.ACCRUED))
                .forEach(row -> {
                    String level = row.getLevel() == null ? PartnerCommission.LEVEL_DIRECT : row.getLevel();
                    if (PartnerCommission.LEVEL_INDIRECT.equals(level)) {
                        indirect.merge(row.getPartnerId(), row.getCommissionAmount(), BigDecimal::add);
                    } else if (PartnerCommission.LEVEL_OTHER.equals(level)) {
                        other.merge(row.getPartnerId(), row.getCommissionAmount(), BigDecimal::add);
                    } else {
                        direct.merge(row.getPartnerId(), row.getCommissionAmount(), BigDecimal::add);
                        deals.merge(row.getPartnerId(), 1L, Long::sum);
                    }
                });
        Map<Long, BigDecimal> paid = new HashMap<>();
        withdrawMapper.selectList(Wrappers.<PartnerWithdraw>lambdaQuery()
                        .in(PartnerWithdraw::getPartnerId, ids)
                        .eq(PartnerWithdraw::getStatus, PartnerWithdraw.PAID))
                .forEach(row -> paid.merge(row.getPartnerId(), row.getAmount(), BigDecimal::add));
        List<Partner> children = partnerMapper.selectList(Wrappers.<Partner>lambdaQuery()
                .in(Partner::getParentId, ids));
        Map<Long, Long> childParent = new HashMap<>();
        children.forEach(child -> childParent.put(child.getId(), child.getParentId()));
        Set<Long> ownerIds = new HashSet<>(ids);
        ownerIds.addAll(childParent.keySet());
        Map<Long, Integer> directCount = new HashMap<>();
        Map<Long, Integer> indirectCount = new HashMap<>();
        if (!ownerIds.isEmpty()) {
            customerMapper.selectList(Wrappers.<Customer>lambdaQuery().in(Customer::getPartnerId, ownerIds))
                    .forEach(customer -> {
                        if (ids.contains(customer.getPartnerId())) {
                            Partner self = rows.stream().filter(row -> row.getId().equals(customer.getPartnerId())).findFirst().orElse(null);
                            if (self != null && self.getCustomerId().equals(customer.getId())) {
                                return;
                            }
                            directCount.merge(customer.getPartnerId(), 1, Integer::sum);
                        }
                        Long parentId = childParent.get(customer.getPartnerId());
                        if (parentId != null) {
                            Partner child = children.stream().filter(row -> row.getId().equals(customer.getPartnerId())).findFirst().orElse(null);
                            if (child != null && child.getCustomerId().equals(customer.getId())) {
                                return;
                            }
                            indirectCount.merge(parentId, 1, Integer::sum);
                        }
                    });
        }
        Map<Long, String> parentNames = new HashMap<>();
        List<Long> parentIds = rows.stream().map(Partner::getParentId).filter(id -> id != null).distinct().toList();
        if (!parentIds.isEmpty()) {
            List<Partner> parents = partnerMapper.selectBatchIds(parentIds);
            fillIdentity(parents);
            parents.forEach(parent -> parentNames.put(parent.getId(), parent.getName()));
        }
        rows.forEach(row -> {
            BigDecimal d = direct.getOrDefault(row.getId(), BigDecimal.ZERO);
            BigDecimal i = indirect.getOrDefault(row.getId(), BigDecimal.ZERO);
            BigDecimal o = other.getOrDefault(row.getId(), BigDecimal.ZERO);
            BigDecimal total = d.add(i).add(o);
            BigDecimal taken = paid.getOrDefault(row.getId(), BigDecimal.ZERO);
            row.setDirectAmount(d);
            row.setIndirectAmount(i);
            row.setOtherAmount(o);
            row.setCommissionAmount(total);
            row.setWithdrawnAmount(taken);
            row.setBalanceAmount(total.subtract(taken));
            row.setDealCount(deals.getOrDefault(row.getId(), 0L));
            row.setDirectCount(directCount.getOrDefault(row.getId(), 0));
            row.setIndirectCount(indirectCount.getOrDefault(row.getId(), 0));
            row.setParentName(parentNames.get(row.getParentId()));
        });
    }

    private Long validParent(Long selfId, Long parentId) {
        if (parentId == null) {
            return null;
        }
        Partner parent = must(parentId);
        if (parent.getStatus() == null || parent.getStatus() != 1) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "上级合伙人已停用");
        }
        Long cursor = parentId;
        for (int i = 0; i < 8 && cursor != null; i++) {
            if (cursor.equals(selfId)) {
                throw new BizException(ErrorCode.OPERATION_FAILED, "不能把下级设成上级");
            }
            Partner current = partnerMapper.selectById(cursor);
            cursor = current == null ? null : current.getParentId();
        }
        return parentId;
    }

    private PartnerWithdraw createWithdraw(Long partnerId, BigDecimal amount, String remark, String status) {
        if (amount == null || amount.signum() <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "提现金额要大于 0");
        }
        BigDecimal value = amount.setScale(2, RoundingMode.HALF_UP);
        Partner partner = must(partnerId);
        fillStats(List.of(partner));
        BigDecimal available = nz(partner.getBalanceAmount()).subtract(pendingAmount(partnerId));
        if (value.compareTo(available) > 0) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "超过可提现余额");
        }
        PartnerWithdraw row = new PartnerWithdraw();
        row.setPartnerId(partnerId);
        row.setAmount(value);
        row.setStatus(status);
        row.setRemark(StringUtils.hasText(remark) ? remark.trim() : null);
        withdrawMapper.insert(row);
        return row;
    }

    private BigDecimal pendingAmount(Long partnerId) {
        return withdrawMapper.selectList(Wrappers.<PartnerWithdraw>lambdaQuery()
                        .eq(PartnerWithdraw::getPartnerId, partnerId)
                        .eq(PartnerWithdraw::getStatus, PartnerWithdraw.PENDING))
                .stream().map(PartnerWithdraw::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void fillIdentity(List<Partner> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<Long> ids = rows.stream().map(Partner::getCustomerId).distinct().toList();
        Map<Long, Customer> customers = new HashMap<>();
        customerMapper.selectBatchIds(ids).forEach(customer -> customers.put(customer.getId(), customer));
        rows.forEach(row -> {
            Customer customer = customers.get(row.getCustomerId());
            if (customer != null) {
                row.setName(StringUtils.hasText(customer.getName()) ? customer.getName() : "未命名");
                row.setPhone(customer.getPhone());
            }
        });
    }

    private void fillDealCustomers(List<PartnerCommission> deals) {
        if (deals.isEmpty()) {
            return;
        }
        List<Long> ids = deals.stream().map(PartnerCommission::getCustomerId).distinct().toList();
        Map<Long, Customer> customers = new HashMap<>();
        customerMapper.selectBatchIds(ids).forEach(customer -> customers.put(customer.getId(), customer));
        deals.forEach(row -> {
            Customer customer = customers.get(row.getCustomerId());
            if (customer != null) {
                row.setCustomerName(StringUtils.hasText(customer.getName()) ? customer.getName() : "未命名");
                row.setCustomerPhone(customer.getPhone());
            }
        });
    }

    private void scrub(Customer customer) {
        customer.setWxOpenid(null);
        customer.setPhoneHash(null);
        customer.setFamilyNote(null);
        customer.setRemark(null);
        customer.setAddress(null);
    }

    private Partner must(Long id) {
        Partner partner = partnerMapper.selectById(id);
        if (partner == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "合伙人不存在");
        }
        return partner;
    }

    private BigDecimal normalizeRate(BigDecimal rate) {
        if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BizException(ErrorCode.BAD_REQUEST, "提成比例在 0 到 100 之间");
        }
        return rate.setScale(2, RoundingMode.HALF_UP);
    }

    private String newCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        for (int attempt = 0; attempt < 8; attempt++) {
            StringBuilder code = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                code.append(alphabet.charAt(ThreadLocalRandom.current().nextInt(alphabet.length())));
            }
            Long used = partnerMapper.selectCount(Wrappers.<Partner>lambdaQuery().eq(Partner::getCode, code.toString()));
            if (used == null || used == 0) {
                return code.toString();
            }
        }
        throw new BizException(ErrorCode.OPERATION_FAILED, "分享码生成失败，请再试一次");
    }
}
