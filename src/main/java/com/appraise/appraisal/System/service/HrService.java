package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.AppraisalResponse;
import com.appraise.appraisal.System.dtos.CreateAppraisalRequest;
import com.appraise.appraisal.System.dtos.CycleReportResponse;
import com.appraise.appraisal.System.dtos.DashboardResponse;
import com.appraise.appraisal.System.dtos.GoalRequest;
import com.appraise.appraisal.System.dtos.GoalResponse;

import java.util.List;

public interface HrService {

    DashboardResponse getDashboard();

    AppraisalResponse createSingleAppraisal(CreateAppraisalRequest request);

    // Returns every appraisal actually created; employees who already had
    // one for the chosen cycle are silently skipped.
    List<AppraisalResponse> createAppraisalsForDepartment(CreateAppraisalRequest request);

    List<AppraisalResponse> createAppraisalsForAllEmployees(CreateAppraisalRequest request);

    AppraisalResponse advanceAppraisalStatus(Long appraisalId);

    CycleReportResponse getCycleReport(Long cycleId);

    // ---- Goal management (HR assigns goals to managers) ----

    List<GoalResponse> getAllGoals();

    // Appraisals belonging to users with MANAGER role — the pool HR picks
    // from when assigning a goal.
    List<AppraisalResponse> getAssignableAppraisals();

    GoalResponse createGoal(GoalRequest request);

    void deleteGoal(Long goalId);

    GoalResponse confirmGoalStatus(Long goalId, boolean completed);
}