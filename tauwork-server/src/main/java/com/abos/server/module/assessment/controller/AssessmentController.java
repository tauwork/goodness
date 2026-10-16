package com.abos.server.module.assessment.controller;

import com.abos.server.common.result.Result;
import com.abos.server.module.assessment.service.AssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能评估（公开）：
 *  GET  /api/mini/assess/quiz    题库（分组渲染）
 *  POST /api/mini/assess/submit  提交 {answers:{code:[optionId]}} → 分数+推荐+自动建线索
 */
@RestController
@RequestMapping("/api/mini/assess")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping("/quiz")
    public Result<Map<String, Object>> quiz() {
        return Result.ok(assessmentService.quizView());
    }

    @PostMapping("/submit")
    public Result<Map<String, Object>> submit(@RequestBody Map<String, List<Long>> answers) {
        return Result.ok(assessmentService.submit(null, null, answers));
    }
}
