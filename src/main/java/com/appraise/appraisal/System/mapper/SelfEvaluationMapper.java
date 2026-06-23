package com.appraise.appraisal.System.mapper;

import com.appraise.appraisal.System.dtos.SelfEvaluationResponse;
import com.appraise.appraisal.System.entity.SelfEvaluation;
import org.springframework.stereotype.Component;

@Component
public class SelfEvaluationMapper {

    public SelfEvaluationResponse toResponse(SelfEvaluation selfEvaluation) {
        if (selfEvaluation == null) {
            return null;
        }

        SelfEvaluationResponse response = new SelfEvaluationResponse();
        response.setId(selfEvaluation.getId());
        response.setAchievements(selfEvaluation.getAchievements());
        response.setChallenges(selfEvaluation.getChallenges());
        response.setComments(selfEvaluation.getComments());
        response.setCreatedAt(selfEvaluation.getCreatedAt());
        response.setUpdatedAt(selfEvaluation.getUpdatedAt());

        if (selfEvaluation.getUser() != null) {
            response.setUserId(selfEvaluation.getUser().getId());
            response.setEmployeeName(selfEvaluation.getUser().getName());
        }

        if (selfEvaluation.getAppraisalCycle() != null) {
            response.setAppraisalCycleId(selfEvaluation.getAppraisalCycle().getId());
            response.setCycleName(selfEvaluation.getAppraisalCycle().getName());
        }

        return response;
    }
}