package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.AppraisalResponse;
import com.appraise.appraisal.System.dtos.CreateAppraisalRequest;
import com.appraise.appraisal.System.dtos.CycleReportResponse;
import com.appraise.appraisal.System.dtos.DashboardResponse;

import java.util.List;

public interface HrService {

    DashboardResponse getDashboard();

    AppraisalResponse createSingleAppraisal(CreateAppraisalRequest request);

    // Returns every appraisal actually created; employees who already had
    // one for the chosen cycle are silently skipped (matches the frontend's
    // mock behavior — see CreateAppraisalRequest javadoc and HrController).
    List<AppraisalResponse> createAppraisalsForDepartment(CreateAppraisalRequest request);

    List<AppraisalResponse> createAppraisalsForAllEmployees(CreateAppraisalRequest request);

    AppraisalResponse advanceAppraisalStatus(Long appraisalId);

    CycleReportResponse getCycleReport(Long cycleId);
}