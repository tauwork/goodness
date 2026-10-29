package com.abos.server.module.coreg.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.coreg.dto.CoRegKycDTO;
import com.abos.server.module.coreg.dto.CoRegMemberDTO;
import com.abos.server.module.coreg.dto.CoRegNameDTO;
import com.abos.server.module.coreg.dto.CoRegProfileDTO;
import com.abos.server.module.coreg.dto.CoRegReviewDTO;
import com.abos.server.module.coreg.entity.CoRegCase;
import com.abos.server.module.coreg.entity.CoRegLog;
import com.abos.server.module.coreg.entity.CoRegMember;
import com.abos.server.module.coreg.mapper.CoRegCaseMapper;
import com.abos.server.module.coreg.mapper.CoRegLogMapper;
import com.abos.server.module.coreg.mapper.CoRegMemberMapper;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.mapper.CustomerMapper;
import com.abos.server.module.file.config.UploadProperties;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.svcorder.entity.SvcOrder;
import com.abos.server.module.svcorder.mapper.SvcOrderMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 香港公司注册工单。客户在线下资料，运营按核名、资料、身份、签署、递交、领证核对。
 */
@Service
@RequiredArgsConstructor
public class CoRegService {

    private static final List<String> STEP_ORDER = List.of(
            CoRegCase.NAME, CoRegCase.PROFILE, CoRegCase.KYC,
            CoRegCase.SIGN, CoRegCase.FILING, CoRegCase.CERT, CoRegCase.DONE);
    private static final Map<String, String> STEP_LABEL = Map.of(
            CoRegCase.NAME, "公司核名",
            CoRegCase.PROFILE, "公司资料",
            CoRegCase.KYC, "身份资料",
            CoRegCase.SIGN, "签署文件",
            CoRegCase.FILING, "递交注册处",
            CoRegCase.CERT, "领取证书",
            CoRegCase.DONE, "完成交付");
    private static final BigDecimal DEFAULT_CAPITAL = new BigDecimal("10000");
    private static final BigDecimal FULL = new BigDecimal("100");
    private static final BigDecimal RATIO_TOLERANCE = new BigDecimal("0.05");

    private final CoRegCaseMapper caseMapper;
    private final CoRegMemberMapper memberMapper;
    private final CoRegLogMapper logMapper;
    private final SvcOrderMapper svcOrderMapper;
    private final CustomerMapper customerMapper;
    private final UploadProperties uploadProperties;

    @Transactional
    public void open(SvcOrder order, SvcProduct product) {
        if (order == null || product == null || !companyRegistration(product)) {
            return;
        }
        Long exists = caseMapper.selectCount(Wrappers.<CoRegCase>lambdaQuery()
                .eq(CoRegCase::getOrderNo, order.getOrderNo()));
        if (exists != null && exists > 0) {
            return;
        }
        CoRegCase row = new CoRegCase();
        row.setOrderNo(order.getOrderNo());
        row.setSvcOrderId(order.getId());
        row.setCustomerId(order.getCustomerId());
        row.setProductId(product.getId());
        row.setProductName(product.getName());
        row.setStep(CoRegCase.NAME);
        row.setStepStatus(CoRegCase.WAIT_USER);
        row.setCaseStatus(SvcOrder.CANCELED.equals(order.getStatus()) ? CoRegCase.CANCELED : CoRegCase.ACTIVE);
        row.setCapitalAmount(DEFAULT_CAPITAL);
        row.setCapitalCurrency("HKD");
        caseMapper.insert(row);
        writeLog(row.getId(), CoRegCase.NAME, "OPEN", "公司注册工单已建立");
    }

    @Transactional
    public void cancel(String orderNo) {
        CoRegCase row = caseMapper.selectOne(Wrappers.<CoRegCase>lambdaQuery().eq(CoRegCase::getOrderNo, orderNo));
        if (row == null || CoRegCase.CASE_DONE.equals(row.getCaseStatus())) {
            return;
        }
        row.setCaseStatus(CoRegCase.CANCELED);
        caseMapper.updateById(row);
        writeLog(row.getId(), row.getStep(), "CANCEL", "订单已取消");
    }

    public List<Map<String, Object>> my(Long customerId) {
        List<CoRegCase> rows = caseMapper.selectList(Wrappers.<CoRegCase>lambdaQuery()
                .eq(CoRegCase::getCustomerId, customerId)
                .orderByDesc(CoRegCase::getCreateTime));
        return rows.stream().map(this::brief).toList();
    }

    public Map<String, Object> detail(Long customerId, Long id) {
        return view(owned(customerId, id), true);
    }

    @Transactional
    public Map<String, Object> submitName(Long customerId, Long id, CoRegNameDTO dto) {
        CoRegCase row = editable(customerId, id, CoRegCase.NAME);
        String name = trim(dto == null ? null : dto.getCompanyName());
        String nameEn = trim(dto == null ? null : dto.getCompanyNameEn());
        if (!StringUtils.hasText(name) && !StringUtils.hasText(nameEn)) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "请填写公司名称");
        }
        row.setCompanyName(name == null ? "" : name);
        row.setCompanyNameEn(nameEn == null ? "" : nameEn);
        row.setStepStatus(CoRegCase.WAIT_REVIEW);
        row.setRemark("");
        caseMapper.updateById(row);
        writeLog(row.getId(), CoRegCase.NAME, "SUBMIT", displayName(row));
        return detail(customerId, id);
    }

    @Transactional
    public Map<String, Object> submitProfile(Long customerId, Long id, CoRegProfileDTO dto) {
        CoRegCase row = editable(customerId, id, CoRegCase.PROFILE);
        if (dto == null || !StringUtils.hasText(dto.getBizScope())) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "请填写主营业务");
        }
        BigDecimal capital = dto.getCapitalAmount() == null ? DEFAULT_CAPITAL : dto.getCapitalAmount();
        if (capital.signum() <= 0) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "注册资金须大于 0");
        }
        List<CoRegMember> members = normalizeMembers(dto.getMembers());
        row.setBizScope(dto.getBizScope().trim());
        row.setCapitalAmount(capital);
        row.setCapitalCurrency("HKD");
        row.setStepStatus(CoRegCase.WAIT_REVIEW);
        row.setRemark("");
        caseMapper.updateById(row);
        memberMapper.delete(Wrappers.<CoRegMember>lambdaQuery().eq(CoRegMember::getCaseId, row.getId()));
        int sort = 0;
        for (CoRegMember member : members) {
            member.setCaseId(row.getId());
            member.setSortNo(sort++);
            memberMapper.insert(member);
        }
        writeLog(row.getId(), CoRegCase.PROFILE, "SUBMIT", "主营业务与股东董事已提交");
        return detail(customerId, id);
    }

    @Transactional
    public Map<String, Object> submitKyc(Long customerId, Long id, CoRegKycDTO dto) {
        CoRegCase row = editable(customerId, id, CoRegCase.KYC);
        List<CoRegMember> members = membersOf(row.getId());
        if (members.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "请先完成公司资料");
        }
        Map<Long, CoRegMemberDTO> incoming = new LinkedHashMap<>();
        if (dto != null && dto.getMembers() != null) {
            for (CoRegMemberDTO item : dto.getMembers()) {
                if (item != null && item.getId() != null) {
                    incoming.put(item.getId(), item);
                }
            }
        }
        for (CoRegMember member : members) {
            CoRegMemberDTO item = incoming.get(member.getId());
            if (item == null) {
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), member.getName() + "的身份资料未提交");
            }
            String front = fileUrl(item.getIdFrontUrl(), member.getName() + "的身份证正面");
            String back = fileUrl(item.getIdBackUrl(), member.getName() + "的身份证反面");
            String hold = fileUrl(item.getIdHoldUrl(), member.getName() + "的手持身份证照片");
            member.setIdFrontUrl(front);
            member.setIdBackUrl(back);
            member.setIdHoldUrl(hold);
            memberMapper.updateById(member);
        }
        row.setStepStatus(CoRegCase.WAIT_REVIEW);
        row.setRemark("");
        caseMapper.updateById(row);
        writeLog(row.getId(), CoRegCase.KYC, "SUBMIT", "身份资料已提交");
        return detail(customerId, id);
    }

    @Transactional
    public Map<String, Object> confirmSign(Long customerId, Long id) {
        CoRegCase row = owned(customerId, id);
        ensureActive(row);
        if (!CoRegCase.SIGN.equals(row.getStep()) || !CoRegCase.WAIT_USER.equals(row.getStepStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "注册文件备妥后才能确认签署");
        }
        row.setStep(CoRegCase.FILING);
        row.setStepStatus(CoRegCase.WAIT_OPS);
        row.setRemark("");
        caseMapper.updateById(row);
        writeLog(row.getId(), CoRegCase.SIGN, "SIGN", "客户已确认签署");
        return detail(customerId, id);
    }

    public List<Map<String, Object>> adminList(String step) {
        List<CoRegCase> rows = caseMapper.selectList(Wrappers.<CoRegCase>lambdaQuery()
                .eq(StringUtils.hasText(step), CoRegCase::getStep, step)
                .orderByDesc(CoRegCase::getCreateTime));
        Map<Long, Customer> customers = customers(rows);
        Map<Long, SvcOrder> orders = orders(rows);
        List<Map<String, Object>> list = new ArrayList<>();
        for (CoRegCase row : rows) {
            Map<String, Object> item = brief(row);
            Customer customer = customers.get(row.getCustomerId());
            if (customer != null) {
                item.put("customerName", customer.getName());
                item.put("customerPhone", customer.getPhone());
            }
            SvcOrder order = orders.get(row.getSvcOrderId());
            if (order != null) {
                item.put("payStatus", order.getStatus());
            }
            list.add(item);
        }
        return list;
    }

    public Map<String, Object> adminDetail(Long id) {
        CoRegCase row = must(id);
        Map<String, Object> view = view(row, true);
        Customer customer = customerMapper.selectById(row.getCustomerId());
        if (customer != null) {
            view.put("customerName", customer.getName());
            view.put("customerPhone", customer.getPhone());
        }
        SvcOrder order = svcOrderMapper.selectById(row.getSvcOrderId());
        if (order != null) {
            view.put("payStatus", order.getStatus());
            view.put("amount", order.getAmount());
        }
        return view;
    }

    @Transactional
    public Map<String, Object> review(Long id, CoRegReviewDTO dto) {
        CoRegCase row = must(id);
        ensureActive(row);
        boolean pass = dto != null && Boolean.TRUE.equals(dto.getPass());
        String remark = trim(dto == null ? null : dto.getRemark());
        String step = row.getStep();
        String status = row.getStepStatus();
        if (CoRegCase.NAME.equals(step) || CoRegCase.PROFILE.equals(step) || CoRegCase.KYC.equals(step)) {
            if (!CoRegCase.WAIT_REVIEW.equals(status)) {
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "客户还未提交，不能核对");
            }
            if (pass) {
                advance(row);
                row.setRemark("");
                writeLog(row.getId(), step, "PASS", remark);
            } else {
                if (!StringUtils.hasText(remark)) {
                    throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "请填写退回原因");
                }
                row.setStepStatus(CoRegCase.REJECTED);
                row.setRemark(remark);
                writeLog(row.getId(), step, "REJECT", remark);
            }
        } else if (CoRegCase.SIGN.equals(step)) {
            if (!pass || !CoRegCase.WAIT_OPS.equals(status)) {
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "请先将注册文件标记为已备妥");
            }
            row.setStepStatus(CoRegCase.WAIT_USER);
            row.setRemark(StringUtils.hasText(remark) ? remark : "注册文件已备妥，请确认签署");
            writeLog(row.getId(), step, "READY", row.getRemark());
        } else if (CoRegCase.FILING.equals(step) || CoRegCase.CERT.equals(step)) {
            if (!pass || !CoRegCase.WAIT_OPS.equals(status)) {
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "当前步骤由运营推进");
            }
            advance(row);
            row.setRemark("");
            writeLog(row.getId(), step, "PASS", remark);
        } else {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "工单已完成");
        }
        caseMapper.updateById(row);
        return adminDetail(id);
    }

    private void advance(CoRegCase row) {
        switch (row.getStep()) {
            case CoRegCase.NAME -> move(row, CoRegCase.PROFILE, CoRegCase.WAIT_USER);
            case CoRegCase.PROFILE -> move(row, CoRegCase.KYC, CoRegCase.WAIT_USER);
            case CoRegCase.KYC -> move(row, CoRegCase.SIGN, CoRegCase.WAIT_OPS);
            case CoRegCase.FILING -> move(row, CoRegCase.CERT, CoRegCase.WAIT_OPS);
            case CoRegCase.CERT -> {
                row.setStep(CoRegCase.DONE);
                row.setStepStatus(CoRegCase.STEP_DONE);
                row.setCaseStatus(CoRegCase.CASE_DONE);
            }
            default -> throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "当前步骤不能通过");
        }
    }

    private void move(CoRegCase row, String step, String status) {
        row.setStep(step);
        row.setStepStatus(status);
    }

    private boolean companyRegistration(SvcProduct product) {
        return product.getName() != null && product.getName().contains("公司注册");
    }

    private List<CoRegMember> normalizeMembers(List<CoRegMemberDTO> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "请填写股东及董事");
        }
        List<CoRegMember> members = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;
        boolean holder = false;
        boolean director = false;
        for (CoRegMemberDTO item : incoming) {
            String name = trim(item == null ? null : item.getName());
            String role = item == null ? null : item.getRole();
            if (!StringUtils.hasText(name)) {
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "请填写姓名");
            }
            CoRegMember member = new CoRegMember();
            member.setName(name);
            if (CoRegMember.DIRECTOR.equals(role)) {
                director = true;
                member.setRole(role);
                member.setShareRatio(BigDecimal.ZERO);
            } else if (CoRegMember.SHAREHOLDER.equals(role) || CoRegMember.BOTH.equals(role)) {
                holder = true;
                if (CoRegMember.BOTH.equals(role)) {
                    director = true;
                }
                BigDecimal ratio = item.getShareRatio();
                if (ratio == null || ratio.signum() <= 0 || ratio.compareTo(FULL) > 0) {
                    throw new BizException(ErrorCode.BAD_REQUEST.getCode(), name + "的持股比例不正确");
                }
                member.setRole(role);
                member.setShareRatio(ratio);
                sum = sum.add(ratio);
            } else {
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "请选择股东或董事身份");
            }
            members.add(member);
        }
        if (!holder) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "至少需要一名股东");
        }
        if (!director) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "至少需要一名董事");
        }
        if (sum.subtract(FULL).abs().compareTo(RATIO_TOLERANCE) > 0) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "股东持股比例合计须为 100%");
        }
        return members;
    }

    private String fileUrl(String url, String label) {
        String prefix = uploadProperties.getUrlPrefix();
        if (!StringUtils.hasText(url) || !url.startsWith(prefix)) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "请上传" + label);
        }
        return url;
    }

    private CoRegCase editable(Long customerId, Long id, String step) {
        CoRegCase row = owned(customerId, id);
        ensureActive(row);
        if (!step.equals(row.getStep())) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "当前不是这一步");
        }
        if (!CoRegCase.WAIT_USER.equals(row.getStepStatus()) && !CoRegCase.REJECTED.equals(row.getStepStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "资料已提交，请等待核对");
        }
        return row;
    }

    private void ensureActive(CoRegCase row) {
        if (CoRegCase.CANCELED.equals(row.getCaseStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "订单已取消");
        }
        if (CoRegCase.CASE_DONE.equals(row.getCaseStatus())) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "工单已完成");
        }
    }

    private CoRegCase owned(Long customerId, Long id) {
        CoRegCase row = must(id);
        if (!row.getCustomerId().equals(customerId)) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return row;
    }

    private CoRegCase must(Long id) {
        CoRegCase row = caseMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND.getCode(), "工单不存在");
        }
        return row;
    }

    private List<CoRegMember> membersOf(Long caseId) {
        return memberMapper.selectList(Wrappers.<CoRegMember>lambdaQuery()
                .eq(CoRegMember::getCaseId, caseId)
                .orderByAsc(CoRegMember::getSortNo)
                .orderByAsc(CoRegMember::getId));
    }

    private Map<String, Object> brief(CoRegCase row) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", row.getId());
        item.put("orderNo", row.getOrderNo());
        item.put("productName", row.getProductName());
        item.put("companyName", displayName(row));
        item.put("step", row.getStep());
        item.put("stepLabel", STEP_LABEL.get(row.getStep()));
        item.put("stepStatus", row.getStepStatus());
        item.put("stepStatusLabel", statusLabel(row));
        item.put("caseStatus", row.getCaseStatus());
        item.put("createTime", row.getCreateTime());
        return item;
    }

    private Map<String, Object> view(CoRegCase row, boolean withLogs) {
        Map<String, Object> view = brief(row);
        view.put("companyName", row.getCompanyName());
        view.put("companyNameEn", row.getCompanyNameEn());
        view.put("displayName", displayName(row));
        view.put("bizScope", row.getBizScope());
        view.put("capitalAmount", row.getCapitalAmount() == null ? DEFAULT_CAPITAL : row.getCapitalAmount());
        view.put("capitalCurrency", row.getCapitalCurrency() == null ? "HKD" : row.getCapitalCurrency());
        view.put("remark", row.getRemark());
        view.put("members", membersOf(row.getId()));
        view.put("steps", timeline(row));
        if (withLogs) {
            view.put("logs", logMapper.selectList(Wrappers.<CoRegLog>lambdaQuery()
                    .eq(CoRegLog::getCaseId, row.getId())
                    .orderByAsc(CoRegLog::getCreateTime)));
        }
        return view;
    }

    private List<Map<String, String>> timeline(CoRegCase row) {
        int current = STEP_ORDER.indexOf(row.getStep());
        boolean finished = CoRegCase.CASE_DONE.equals(row.getCaseStatus());
        List<Map<String, String>> steps = new ArrayList<>();
        for (int i = 0; i < STEP_ORDER.size(); i++) {
            String code = STEP_ORDER.get(i);
            String state;
            if (finished || i < current) {
                state = "done";
            } else if (i == current) {
                state = "current";
            } else {
                state = "wait";
            }
            Map<String, String> item = new LinkedHashMap<>();
            item.put("code", code);
            item.put("label", STEP_LABEL.get(code));
            item.put("state", state);
            steps.add(item);
        }
        return steps;
    }

    private String statusLabel(CoRegCase row) {
        if (CoRegCase.CANCELED.equals(row.getCaseStatus())) {
            return "已取消";
        }
        if (CoRegCase.CASE_DONE.equals(row.getCaseStatus())) {
            return "已完成";
        }
        return switch (row.getStepStatus()) {
            case CoRegCase.WAIT_USER -> "待您填写";
            case CoRegCase.WAIT_REVIEW -> "待核对";
            case CoRegCase.REJECTED -> "需修改";
            case CoRegCase.WAIT_OPS -> "运营办理中";
            default -> row.getStepStatus();
        };
    }

    private String displayName(CoRegCase row) {
        if (StringUtils.hasText(row.getCompanyName())) {
            return row.getCompanyName();
        }
        return StringUtils.hasText(row.getCompanyNameEn()) ? row.getCompanyNameEn() : "待填写名称";
    }

    private Map<Long, Customer> customers(List<CoRegCase> rows) {
        List<Long> ids = rows.stream().map(CoRegCase::getCustomerId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, Customer> map = new LinkedHashMap<>();
        for (Customer customer : customerMapper.selectBatchIds(ids)) {
            map.put(customer.getId(), customer);
        }
        return map;
    }

    private Map<Long, SvcOrder> orders(List<CoRegCase> rows) {
        List<Long> ids = rows.stream().map(CoRegCase::getSvcOrderId).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, SvcOrder> map = new LinkedHashMap<>();
        for (SvcOrder order : svcOrderMapper.selectBatchIds(ids)) {
            map.put(order.getId(), order);
        }
        return map;
    }

    private void writeLog(Long caseId, String step, String action, String remark) {
        CoRegLog log = new CoRegLog();
        log.setCaseId(caseId);
        log.setStep(step);
        log.setAction(action);
        log.setRemark(trim(remark));
        logMapper.insert(log);
    }

    private String trim(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }
}
