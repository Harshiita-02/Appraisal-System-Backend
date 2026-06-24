package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.*;

import java.util.List;

public interface ManagerService {

    // NOTE: same temporary-managerId-parameter pattern as EmployeeService —
    // see the comment there for what changes once JWT auth is added.

    ManagerDashboardResponse getDashboard(Long managerId);

    List<TeamMemberResponse> getTeam(Long managerId);

    List<GoalResponse> getTeamGoals(Long managerId);

    // Team's appraisals — the dropdown source when attaching a new goal.
    List<AppraisalResponse> getAssignableAppraisals(Long managerId);

    // --- Manager-as-employee routes (their own appraisal, as someone else's report) ---
    List<AppraisalResponse> getMyAppraisals(Long managerId);
    AppraisalResponse submitSelfAssessment(Long managerId, Long appraisalId, SelfAssessmentRequest request);
    AppraisalResponse saveSelfAssessmentDraft(Long managerId, Long appraisalId, SelfAssessmentRequest request);
    List<GoalResponse> getMyGoals(Long managerId);
    GoalResponse respondToGoal(Long managerId, Long goalId, EmployeeGoalCompletionRequest request);

    // --- Manager-of-team routes ---
    GoalResponse createGoal(Long managerId, GoalRequest request);
    void deleteGoal(Long managerId, Long goalId);
    GoalResponse confirmGoalStatus(Long managerId, Long goalId, boolean completed);

    TeamReportResponse getTeamReport(Long managerId, Long cycleId);

    AppraisalResponse reviewTeamAppraisal(Long managerId, Long appraisalId, ManagerReviewRequest request, boolean submit);
}