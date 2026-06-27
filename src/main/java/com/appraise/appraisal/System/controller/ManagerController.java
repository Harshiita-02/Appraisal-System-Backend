package com.appraise.appraisal.System.controller;

import com.appraise.appraisal.System.config.security.AuthenticatedUser;
import com.appraise.appraisal.System.dtos.*;
import com.appraise.appraisal.System.service.ManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;
    private final AuthenticatedUser authenticatedUser;

    @GetMapping("/dashboard")
    public ResponseEntity<ManagerDashboardResponse> getDashboard() {
        return ResponseEntity.ok(managerService.getDashboard(authenticatedUser.getId()));
    }

    @GetMapping("/team")
    public ResponseEntity<List<TeamMemberResponse>> getTeam() {
        return ResponseEntity.ok(managerService.getTeam(authenticatedUser.getId()));
    }

    @GetMapping("/goals")
    public ResponseEntity<List<GoalResponse>> getTeamGoals() {
        return ResponseEntity.ok(managerService.getTeamGoals(authenticatedUser.getId()));
    }

    @GetMapping("/appraisals")
    public ResponseEntity<List<AppraisalResponse>> getAssignableAppraisals() {
        return ResponseEntity.ok(managerService.getAssignableAppraisals(authenticatedUser.getId()));
    }

    @GetMapping("/my-appraisals")
    public ResponseEntity<List<AppraisalResponse>> getMyAppraisals() {
        return ResponseEntity.ok(managerService.getMyAppraisals(authenticatedUser.getId()));
    }

    @PostMapping("/my-appraisals/{id}/self-assessment")
    public ResponseEntity<AppraisalResponse> submitSelfAssessment(@PathVariable("id") Long appraisalId,
                                                                  @Valid @RequestBody SelfAssessmentRequest request) {
        return ResponseEntity.ok(managerService.submitSelfAssessment(authenticatedUser.getId(), appraisalId, request));
    }

    @PutMapping("/my-appraisals/{id}/draft")
    public ResponseEntity<AppraisalResponse> saveSelfAssessmentDraft(@PathVariable("id") Long appraisalId,
                                                                     @RequestBody SelfAssessmentRequest request) {
        return ResponseEntity.ok(managerService.saveSelfAssessmentDraft(authenticatedUser.getId(), appraisalId, request));
    }

    @GetMapping("/my-goals")
    public ResponseEntity<List<GoalResponse>> getMyGoals() {
        return ResponseEntity.ok(managerService.getMyGoals(authenticatedUser.getId()));
    }

    @PatchMapping("/my-goals/{id}/respond")
    public ResponseEntity<GoalResponse> respondToGoal(@PathVariable("id") Long goalId,
                                                      @RequestBody EmployeeGoalCompletionRequest request) {
        return ResponseEntity.ok(managerService.respondToGoal(authenticatedUser.getId(), goalId, request));
    }

    @PostMapping("/goals")
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        return ResponseEntity.ok(managerService.createGoal(authenticatedUser.getId(), request));
    }

    @DeleteMapping("/goals/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable("id") Long goalId) {
        managerService.deleteGoal(authenticatedUser.getId(), goalId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/goals/{id}/confirm")
    public ResponseEntity<GoalResponse> confirmGoalStatus(@PathVariable("id") Long goalId,
                                                          @RequestBody ConfirmGoalRequest request) {
        return ResponseEntity.ok(managerService.confirmGoalStatus(authenticatedUser.getId(), goalId, request.isCompleted()));
    }

    @GetMapping("/reports")
    public ResponseEntity<TeamReportResponse> getTeamReport(@RequestParam Long cycleId) {
        return ResponseEntity.ok(managerService.getTeamReport(authenticatedUser.getId(), cycleId));
    }

    @PutMapping("/team-appraisals/{id}/review")
    public ResponseEntity<AppraisalResponse> reviewTeamAppraisal(@PathVariable("id") Long appraisalId,
                                                                 @RequestParam(defaultValue = "true") boolean submit,
                                                                 @Valid @RequestBody ManagerReviewRequest request) {
        return ResponseEntity.ok(managerService.reviewTeamAppraisal(authenticatedUser.getId(), appraisalId, request, submit));
    }
}