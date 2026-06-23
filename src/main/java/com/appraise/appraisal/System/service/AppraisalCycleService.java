package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.AppraisalCycleRequest;
import com.appraise.appraisal.System.dtos.AppraisalCycleResponse;

import java.util.List;

public interface AppraisalCycleService {

    AppraisalCycleResponse createCycle(AppraisalCycleRequest request);

    List<AppraisalCycleResponse> getAllCycles();

    AppraisalCycleResponse getCycleById(Long id);

    AppraisalCycleResponse updateCycle(Long id, AppraisalCycleRequest request);

    void deleteCycle(Long id);
}