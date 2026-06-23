package com.appraise.appraisal.System.controller;

import com.appraise.appraisal.System.dtos.*;
import com.appraise.appraisal.System.service.ManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Same temporary `managerId` query-param pattern as EmployeeController.
 */
@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    @GetMapping("/dashboard")
    public ResponseEntity<ManagerDashboardResponse> getDashboard(@RequestParam Long managerId) {
        return ResponseEntity.ok(managerService.getDashboard(managerId));
    }

    @GetMapping("/team")
    public ResponseEntity<List<TeamMemberResponse>> getTeam(@RequestParam Long managerId) {
        return ResponseEntity.ok(managerService.getTeam(managerId));
    }

    @GetMapping("/goals")
    public ResponseEntity<List<GoalResponse>> getTeamGoals(@RequestParam Long managerId) {
        return ResponseEntity.ok(managerService.getTeamGoals(managerId));
    }

    @GetMapping("/appraisals")
    public ResponseEntity<List<AppraisalResponse>> getAssignableAppraisals(@RequestParam Long managerId) {
        return ResponseEntity.ok(managerService.getAssignableAppraisals(managerId));
    }

    @GetMapping("/my-appraisals")
    public ResponseEntity<List<AppraisalResponse>> getMyAppraisals(@RequestParam Long managerId) {
        return ResponseEntity.ok(managerService.getMyAppraisals(managerId));
    }

    @PostMapping("/my-appraisals/{id}/self-assessment")
    public ResponseEntity<AppraisalResponse> submitSelfAssessment(@RequestParam Long managerId,
                                                                  @PathVariable("id") Long appraisalId,
                                                                  @Valid @RequestBody SelfAssessmentRequest request) {
        return ResponseEntity.ok(managerService.submitSelfAssessment(managerId, appraisalId, request));
    }

    @PutMapping("/my-appraisals/{id}/draft")
    public ResponseEntity<AppraisalResponse> saveSelfAssessmentDraft(@RequestParam Long managerId,
                                                                     @PathVariable("id") Long appraisalId,
                                                                     @RequestBody SelfAssessmentRequest request) {
        return ResponseEntity.ok(managerService.saveSelfAssessmentDraft(managerId, appraisalId, request));
    }

    @GetMapping("/my-goals")
    public ResponseEntity<List<GoalResponse>> getMyGoals(@RequestParam Long managerId) {
        return ResponseEntity.ok(managerService.getMyGoals(managerId));
    }

    @PatchMapping("/my-goals/{id}/respond")
    public ResponseEntity<GoalResponse> respondToGoal(@RequestParam Long managerId,
                                                      @PathVariable("id") Long goalId,
                                                      @RequestBody EmployeeGoalCompletionRequest request) {
        return ResponseEntity.ok(managerService.respondToGoal(managerId, goalId, request));
    }

    @PostMapping("/goals")
    public ResponseEntity<GoalResponse> createGoal(@RequestParam Long managerId,
                                                   @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.ok(managerService.createGoal(managerId, request));
    }

    @DeleteMapping("/goals/{id}")
    public ResponseEntity<Void> deleteGoal(@RequestParam Long managerId, @PathVariable("id") Long goalId) {
        managerService.deleteGoal(managerId, goalId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/goals/{id}/confirm")
    public ResponseEntity<GoalResponse> confirmGoalStatus(@RequestParam Long managerId,
                                                          @PathVariable("id") Long goalId,
                                                          @RequestBody ConfirmGoalRequest request) {
        return ResponseEntity.ok(managerService.confirmGoalStatus(managerId, goalId, request.isCompleted()));
    }

    @GetMapping("/reports")
    public ResponseEntity<TeamReportResponse> getTeamReport(@RequestParam Long managerId,
                                                            @RequestParam Long cycleId) {
        return ResponseEntity.ok(managerService.getTeamReport(managerId, cycleId));
    }
}