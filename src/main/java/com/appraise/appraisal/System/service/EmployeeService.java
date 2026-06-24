package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.AppraisalResponse;
import com.appraise.appraisal.System.dtos.EmployeeDashboardResponse;
import com.appraise.appraisal.System.dtos.EmployeeGoalCompletionRequest;
import com.appraise.appraisal.System.dtos.GoalResponse;
import com.appraise.appraisal.System.dtos.SelfAssessmentRequest;

import java.util.List;

public interface EmployeeService {

    // NOTE: every method here takes employeeId explicitly as a placeholder
    // for what would normally come from the authenticated user's JWT once
    // security is added. Wiring that up means deleting this parameter and
    // pulling it from the SecurityContext inside the controller instead —
    // the service logic itself won't need to change.

    EmployeeDashboardResponse getDashboard(Long employeeId);

    List<AppraisalResponse> getMyAppraisals(Long employeeId);

    AppraisalResponse getMyAppraisalById(Long employeeId, Long appraisalId);

    AppraisalResponse submitSelfAssessment(Long employeeId, Long appraisalId, SelfAssessmentRequest request);

    AppraisalResponse acknowledgeAppraisal(Long employeeId, Long appraisalId);

    List<GoalResponse> getMyGoals(Long employeeId);

    GoalResponse respondToGoal(Long employeeId, Long goalId, EmployeeGoalCompletionRequest request);
    AppraisalResponse saveSelfAssessmentDraft(Long employeeId, Long appraisalId, SelfAssessmentRequest request);
}