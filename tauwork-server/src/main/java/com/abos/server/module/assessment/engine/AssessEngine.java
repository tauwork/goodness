package com.abos.server.module.assessment.engine;

import com.abos.server.module.assessment.entity.AssessOption;
import com.abos.server.module.assessment.entity.AssessRecommend;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 评估计分引擎（纯函数，可单测）：
 *  输入 answers {questionCode: [optionId...]}，输出分数 + 命中标签 + 推荐路径
 */
public class AssessEngine {

    @Data
    public static class Result {
        private int score;                       // 综合适配度（权重总分换算 0-100）
        private Set<String> tags = new HashSet<>();
        private List<Recommend> recommends = new ArrayList<>();
    }

    @Data
    public static class Recommend {
        private String code;
        private String name;
        private Long productId;
        private int fit;                         // 适配度百分比
    }

    /**
     * @param answers     {questionCode: optionIds}
     * @param totalWeight 全卷权重和（换算百分制）
     */
    public static Result evaluate(Map<String, List<Long>> answers,
                                  Map<Long, AssessOption> optionById,
                                  List<AssessRecommend> recommendRules,
                                  int totalWeight) {
        Result r = new Result();
        int raw = 0;
        for (List<Long> optionIds : answers.values()) {
            if (optionIds == null) continue;
            for (Long oid : optionIds) {
                AssessOption o = optionById.get(oid);
                if (o == null) continue;
                raw += o.getScore();
                if (o.getTag() != null) r.getTags().add(o.getTag());
            }
        }
        int full = Math.max(1, totalWeight);
        r.score = Math.min(100, Math.round(raw * 100f / full));

        // 推荐路径：满足 minScore 且命中 requireTag（空则不要求），按 priority 排序取前 3
        recommendRules.stream()
                .filter(rule -> r.score >= rule.getMinScore())
                .filter(rule -> rule.getRequireTag() == null || r.getTags().contains(rule.getRequireTag()))
                .sorted((a, b) -> Integer.compare(a.getPriority(), b.getPriority()))
                .limit(3)
                .forEach(rule -> {
                    Recommend rec = new Recommend();
                    rec.setCode(rule.getCode());
                    rec.setName(rule.getName());
                    rec.setProductId(rule.getProductId());
                    // 适配度：分数与下限的线性映射，上限 99
                    int fit = Math.min(99, rule.getMinScore() == 0 ? 70
                            : Math.round(r.score * 92f / Math.max(1, rule.getMinScore())));
                    rec.setFit(fit);
                    r.getRecommends().add(rec);
                });
        if (r.getRecommends().isEmpty() && !recommendRules.isEmpty()) {
            // 兜底：全部不达标时给最低门槛的路径
            AssessRecommend fallback = recommendRules.stream()
                    .min((a, b) -> Integer.compare(a.getMinScore(), b.getMinScore())).orElse(null);
            if (fallback != null) {
                Recommend rec = new Recommend();
                rec.setCode(fallback.getCode());
                rec.setName(fallback.getName());
                rec.setProductId(fallback.getProductId());
                rec.setFit(Math.max(40, r.score));
                r.getRecommends().add(rec);
            }
        }
        return r;
    }
}
