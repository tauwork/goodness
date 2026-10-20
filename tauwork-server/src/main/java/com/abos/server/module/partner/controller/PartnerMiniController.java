package com.abos.server.module.partner.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.partner.dto.PartnerHomeVO;
import com.abos.server.module.partner.dto.PartnerSaveDTO;
import com.abos.server.module.partner.entity.Partner;
import com.abos.server.module.partner.entity.PartnerWithdraw;
import com.abos.server.module.partner.service.PartnerService;
import com.abos.server.security.LoginContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mini/partner")
@RequiredArgsConstructor
public class PartnerMiniController {

    private final PartnerService partnerService;

    @GetMapping("/me")
    public Result<PartnerHomeVO> me() {
        return Result.ok(partnerService.home(LoginContext.currentCustomerId()));
    }

    @PostMapping("/visit")
    public Result<Void> visit(@RequestBody PartnerSaveDTO dto) {
        partnerService.bindByCode(LoginContext.currentCustomerId(), dto.getCode());
        return Result.ok();
    }

    @PostMapping("/join")
    public Result<Partner> join(@RequestBody PartnerSaveDTO dto) {
        return Result.ok(partnerService.join(LoginContext.currentCustomerId(), dto.getCode()));
    }

    @PostMapping("/withdraw")
    public Result<PartnerWithdraw> withdraw(@RequestBody PartnerSaveDTO dto) {
        return Result.ok(partnerService.requestWithdraw(LoginContext.currentCustomerId(), dto.getAmount()));
    }
}
