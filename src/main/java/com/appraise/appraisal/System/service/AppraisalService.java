package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.AppraisalRequest;
import com.appraise.appraisal.System.dtos.AppraisalResponse;
import com.appraise.appraisal.System.entity.enums.AppraisalStatus;

import java.util.List;

public interface AppraisalService {

    AppraisalResponse createAppraisal(AppraisalRequest request);

    List<AppraisalResponse> getAllAppraisals();

    AppraisalResponse getAppraisalById(Long id);

    AppraisalResponse updateStatus(Long id, AppraisalStatus status);

    void deleteAppraisal(Long id);
}
