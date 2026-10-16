package com.abos.server.module.assessment.service;

import com.abos.server.common.enums.ErrorCode;
import com.abos.server.common.exception.BizException;
import com.abos.server.module.assessment.engine.AssessEngine;
import com.abos.server.module.assessment.entity.*;
import com.abos.server.module.assessment.mapper.*;
import com.abos.server.module.lead.entity.Lead;
import com.abos.server.module.lead.mapper.LeadMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 智能评估：题库加载（分组渲染）→ 提交计分 → 推荐路径 → 自动生成线索进 CRM
 */
@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessQuizMapper quizMapper;
    private final AssessQuestionMapper questionMapper;
    private final AssessOptionMapper optionMapper;
    private final AssessRecommendMapper recommendMapper;
    private final LeadMapper leadMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 题库视图：题组 → 题目 → 选项（公开） */
    public Map<String, Object> quizView() {
        AssessQuiz quiz = quizMapper.selectOne(Wrappers.<AssessQuiz>lambdaQuery()
                .eq(AssessQuiz::getStatus, 1).last("limit 1"));
        if (quiz == null) throw new BizException(ErrorCode.NOT_FOUND, "暂无生效题库");

        List<AssessQuestion> questions = questionMapper.selectList(Wrappers.<AssessQuestion>lambdaQuery()
                .eq(AssessQuestion::getQuizId, quiz.getId())
                .orderByAsc(AssessQuestion::getSeq));
        Map<Long, List<AssessOption>> optionsByQ = optionMapper.selectList(
                        Wrappers.<AssessOption>lambdaQuery()
                                .in(AssessOption::getQuestionId,
                                        questions.stream().map(AssessQuestion::getId).toList())
                                .orderByAsc(AssessOption::getSeq)).stream()
                .collect(Collectors.groupingBy(AssessOption::getQuestionId));

        List<Map<String, Object>> qList = new ArrayList<>();
        int totalWeight = 0;
        for (AssessQuestion q : questions) {
            totalWeight += q.getWeight();
            Map<String, Object> qm = new LinkedHashMap<>();
            qm.put("id", q.getId());
            qm.put("code", q.getCode());
            qm.put("group", q.getGroupName());
            qm.put("title", q.getTitle());
            qm.put("multi", q.getMulti() == 1);
            List<Map<String, Object>> opts = new ArrayList<>();
            for (AssessOption o : optionsByQ.getOrDefault(q.getId(), List.of())) {
                opts.add(Map.of("id", o.getId(), "label", o.getLabel()));
            }
            qm.put("options", opts);
            qList.add(qm);
        }
        return Map.of("version", quiz.getVersion(), "totalWeight", totalWeight, "questions", qList);
    }

    /** 提交评估：answers {questionCode: [optionId]} → 计分 + 推荐 + 建线索 */
    @Transactional
    public Map<String, Object> submit(Long customerId, String openid, Map<String, List<Long>> answers) {
        // 校验答案完备性（单选题必答）
        Map<String, Object> view = quizView();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) view.get("questions");
        Set<Long> validOptionIds = new HashSet<>();
        for (Map<String, Object> q : questions) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> opts = (List<Map<String, Object>>) q.get("options");
            for (Map<String, Object> o : opts) validOptionIds.add(((Number) o.get("id")).longValue());
        }
        for (Map.Entry<String, List<Long>> e : answers.entrySet()) {
            e.getValue().removeIf(id -> !validOptionIds.contains(id));
        }
        // 必答校验：单选题必须作答；多选题（如成就）允许空
        for (Map<String, Object> q : questions) {
            boolean single = !Boolean.TRUE.equals(q.get("multi"));
            List<Long> ans = answers.get((String) q.get("code"));
            if (single && (ans == null || ans.isEmpty())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "题目未作答: " + q.get("code"));
            }
        }

        // 计分
        AssessQuiz quiz = quizMapper.selectOne(Wrappers.<AssessQuiz>lambdaQuery()
                .eq(AssessQuiz::getStatus, 1).last("limit 1"));
        List<AssessOption> options = optionMapper.selectList(Wrappers.<AssessOption>lambdaQuery()
                .in(AssessOption::getId, answers.values().stream().flatMap(List::stream).toList()));
        Map<Long, AssessOption> optionById = options.stream()
                .collect(Collectors.toMap(AssessOption::getId, Function.identity()));
        List<AssessRecommend> rules = recommendMapper.selectList(Wrappers.<AssessRecommend>lambdaQuery()
                .eq(AssessRecommend::getQuizId, quiz.getId())
                .orderByAsc(AssessRecommend::getPriority));
        int totalWeight = (Integer) view.get("totalWeight");

        AssessEngine.Result result = AssessEngine.evaluate(answers, optionById, rules, totalWeight);

        // 生成线索进 CRM（source=ASSESS）
        Lead lead = new Lead();
        lead.setSource("ASSESS");
        lead.setIntent(result.getRecommends().isEmpty() ? null
                : result.getRecommends().get(0).getName());
        lead.setAssessScore(result.getScore());
        lead.setStatus("NEW");
        if (customerId != null) lead.setCustomerId(customerId);
        leadMapper.insert(lead);

        // 返回结果（推荐含 productId 便于前端跳服务详情）
        List<Map<String, Object>> recs = result.getRecommends().stream()
                .map(rc -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("code", rc.getCode());
                    m.put("name", rc.getName());
                    m.put("productId", rc.getProductId());
                    m.put("fit", rc.getFit());
                    return m;
                }).collect(Collectors.toList());
        return Map.of("score", result.getScore(),
                "recommends", recs,
                "leadId", lead.getId(),
                "version", quiz.getVersion());
    }
}
