package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.SelfEvaluationRequest;
import com.appraise.appraisal.System.dtos.SelfEvaluationResponse;
import java.util.List;

public interface SelfEvaluationService {

    SelfEvaluationResponse createSelfEvaluation(SelfEvaluationRequest request);
    List<SelfEvaluationResponse> getAllSelfEvaluations();
    SelfEvaluationResponse getSelfEvaluationById(Long id);
    SelfEvaluationResponse updateSelfEvaluation(Long id, SelfEvaluationRequest request);
    void deleteSelfEvaluation(Long id);
}