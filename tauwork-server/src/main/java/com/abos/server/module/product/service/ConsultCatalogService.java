package com.abos.server.module.product.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.customer.entity.Customer;
import com.abos.server.module.customer.service.CustomerAdminService;
import com.abos.server.module.lead.entity.Lead;
import com.abos.server.module.lead.mapper.LeadMapper;
import com.abos.server.module.product.dto.ConsultCatalogVO;
import com.abos.server.module.product.dto.ConsultSubmitDTO;
import com.abos.server.module.product.entity.ProductCategory;
import com.abos.server.module.product.entity.SvcProduct;
import com.abos.server.module.team.entity.Consultant;
import com.abos.server.module.team.mapper.ConsultantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConsultCatalogService {

    private final ProductCategoryService categoryService;
    private final SvcProductService productService;
    private final ConsultantMapper consultantMapper;
    private final LeadMapper leadMapper;
    private final CustomerAdminService customerAdmin;

    public ConsultCatalogVO catalog() {
        List<ProductCategory> categories = categoryService.listByType(SvcProduct.TYPE_SERVICE, true);
        Map<Long, ProductCategory> cats = new LinkedHashMap<>();
        categories.forEach(c -> cats.put(c.getId(), c));
        List<SvcProduct> products = productService.listOnSale(SvcProduct.TYPE_SERVICE, null);

        ConsultCatalogVO vo = new ConsultCatalogVO();
        Map<String, ConsultCatalogVO.Group> groups = new LinkedHashMap<>();
        groups.put("ALL", group("ALL", "所有服务"));
        groups.put("IDENTITY", group("IDENTITY", "身份规划"));
        groups.put("COMPANY", group("COMPANY", "公司服务"));
        Map<String, Long> consultantIds = new LinkedHashMap<>();
        for (ProductCategory category : categories) {
            String code = groupCode(category);
            if (!groups.containsKey(code)) {
                groups.put(code, group(code, category.getName()));
            }
            if (category.getConsultantId() != null && !"ALL".equals(code)) {
                consultantIds.putIfAbsent(code, category.getConsultantId());
            }
        }
        for (SvcProduct product : products) {
            String code = groupCode(cats.get(product.getCategoryId()));
            if (product.getConsultantId() != null && !"ALL".equals(code)) {
                consultantIds.putIfAbsent(code, product.getConsultantId());
            }
        }
        Map<Long, Consultant> consultants = loadConsultants(consultantIds.values().stream().distinct().toList());
        consultantIds.forEach((code, id) -> {
            ConsultCatalogVO.Group item = groups.get(code);
            if (item != null) {
                item.setConsultant(contact(consultants.get(id)));
            }
        });
        vo.getGroups().addAll(groups.values());
        return vo;
    }

    public void submit(ConsultSubmitDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getName())) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写姓名");
        }
        String phone = dto.getPhone() == null ? "" : dto.getPhone().trim();
        if (!phone.matches("^1\\d{10}$")) {
            throw new BizException(ErrorCode.BAD_REQUEST, "请填写正确的手机号");
        }
        ConsultCatalogVO.Group chosen = resolveGroup(dto);
        Customer customer = customerAdmin.findByPhone(phone);
        if (customer != null && Customer.STAGE_BLACKLIST.equals(customer.getStage())) {
            throw new BizException(ErrorCode.OPERATION_FAILED, "该手机号无法提交预约");
        }
        Lead lead = new Lead();
        lead.setName(dto.getName().trim());
        lead.setPhone(phone);
        lead.setSource("CONSULT");
        String intent = chosen.getName();
        lead.setIntent(intent.length() > 64 ? intent.substring(0, 64) : intent);
        lead.setRemark(trimRemark(dto.getRemark()));
        lead.setStatus("NEW");
        if (customer != null) {
            lead.setCustomerId(customer.getId());
        }
        leadMapper.insert(lead);
    }

    private ConsultCatalogVO.Group resolveGroup(ConsultSubmitDTO dto) {
        if (StringUtils.hasText(dto.getGroup())) {
            return catalog().getGroups().stream()
                    .filter(g -> dto.getGroup().equals(g.getCode()))
                    .findFirst()
                    .orElseThrow(() -> new BizException(ErrorCode.BAD_REQUEST, "请选择服务分类"));
        }
        if (dto.getProductId() != null) {
            SvcProduct product = productService.getOnSale(dto.getProductId(), SvcProduct.TYPE_SERVICE);
            ConsultCatalogVO.Group group = new ConsultCatalogVO.Group();
            group.setCode("PRODUCT");
            group.setName(product.getName());
            return group;
        }
        throw new BizException(ErrorCode.BAD_REQUEST, "请选择服务分类");
    }

    private Map<Long, Consultant> loadConsultants(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, Consultant> map = new LinkedHashMap<>();
        consultantMapper.selectBatchIds(ids).forEach(c -> map.put(c.getId(), c));
        return map;
    }

    private ConsultCatalogVO.Contact contact(Consultant consultant) {
        if (consultant == null) {
            return null;
        }
        ConsultCatalogVO.Contact contact = new ConsultCatalogVO.Contact();
        contact.setName(consultant.getName());
        contact.setTitle(consultant.getTitle());
        contact.setWechat(consultant.getWechat());
        contact.setPhone(consultant.getPhone());
        return contact;
    }

    private String groupCode(ProductCategory category) {
        if (category == null) {
            return "OTHER";
        }
        String code = category.getCode() == null ? "" : category.getCode().toUpperCase();
        String name = category.getName() == null ? "" : category.getName();
        if (code.contains("IDENTITY") || name.contains("身份")) {
            return "IDENTITY";
        }
        if (code.contains("COMPANY") || code.contains("STARTUP") || name.contains("公司")) {
            return "COMPANY";
        }
        return "C" + category.getId();
    }

    private ConsultCatalogVO.Group group(String code, String name) {
        ConsultCatalogVO.Group group = new ConsultCatalogVO.Group();
        group.setCode(code);
        group.setName(name);
        return group;
    }

    private String trimRemark(String remark) {
        if (!StringUtils.hasText(remark)) {
            return null;
        }
        String text = remark.trim();
        return text.length() > 1000 ? text.substring(0, 1000) : text;
    }
}
