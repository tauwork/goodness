package com.abos.server.module.partner.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.abos.server.common.result.Result;
import com.abos.server.module.partner.dto.PartnerDetailVO;
import com.abos.server.module.partner.dto.PartnerSaveDTO;
import com.abos.server.module.partner.entity.Partner;
import com.abos.server.module.partner.entity.PartnerCommission;
import com.abos.server.module.partner.entity.PartnerRule;
import com.abos.server.module.partner.service.PartnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/partner")
@RequiredArgsConstructor
@SaCheckPermission("partner:manage")
public class PartnerAdminController {

    private final PartnerService partnerService;

    @GetMapping("/list")
    public Result<List<Partner>> list() {
        return Result.ok(partnerService.list());
    }

    @GetMapping("/options")
    public Result<List<Partner>> options() {
        return Result.ok(partnerService.options());
    }

    @GetMapping("/{id}")
    public Result<PartnerDetailVO> detail(@PathVariable Long id) {
        return Result.ok(partnerService.detail(id));
    }

    @PostMapping
    public Result<Partner> create(@RequestBody PartnerSaveDTO dto) {
        return Result.ok(partnerService.create(dto));
    }

    @PutMapping("/{id}/status/{status}")
    public Result<Partner> status(@PathVariable Long id, @PathVariable int status) {
        return Result.ok(partnerService.changeStatus(id, status));
    }

    @PutMapping("/customer/{customerId}")
    public Result<Void> assign(@PathVariable Long customerId, @RequestBody PartnerSaveDTO dto) {
        partnerService.assign(customerId, dto.getPartnerId());
        return Result.ok();
    }

    @PostMapping("/{id}/customer")
    public Result<Void> assignPhone(@PathVariable Long id, @RequestBody PartnerSaveDTO dto) {
        partnerService.assignByPhone(id, dto.getPhone());
        return Result.ok();
    }

    @PostMapping("/{id}/deal")
    public Result<PartnerCommission> deal(@PathVariable Long id, @RequestBody PartnerSaveDTO dto) {
        return Result.ok(partnerService.recordDeal(id, dto));
    }

    @PutMapping("/deal/{id}/reverse")
    public Result<Void> reverse(@PathVariable Long id) {
        partnerService.reverseDeal(id);
        return Result.ok();
    }

    @GetMapping("/rule/list")
    public Result<List<PartnerRule>> rules() {
        return Result.ok(partnerService.rules());
    }

    @PostMapping("/rule")
    public Result<PartnerRule> saveRule(@RequestBody PartnerSaveDTO dto) {
        return Result.ok(partnerService.saveRule(dto.getCategoryId(), dto.getRate()));
    }
}
