package com.appraise.appraisal.System.controller;

import com.appraise.appraisal.System.config.security.AuthenticatedUser;
import com.appraise.appraisal.System.dtos.AppraisalResponse;
import com.appraise.appraisal.System.dtos.EmployeeDashboardResponse;
import com.appraise.appraisal.System.dtos.EmployeeGoalCompletionRequest;
import com.appraise.appraisal.System.dtos.GoalResponse;
import com.appraise.appraisal.System.dtos.SelfAssessmentRequest;
import com.appraise.appraisal.System.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final AuthenticatedUser authenticatedUser;

    @GetMapping("/dashboard")
    public ResponseEntity<EmployeeDashboardResponse> getDashboard() {
        return ResponseEntity.ok(employeeService.getDashboard(authenticatedUser.getId()));
    }

    @GetMapping("/appraisals")
    public ResponseEntity<List<AppraisalResponse>> getMyAppraisals() {
        return ResponseEntity.ok(employeeService.getMyAppraisals(authenticatedUser.getId()));
    }

    @GetMapping("/appraisals/{id}")
    public ResponseEntity<AppraisalResponse> getMyAppraisalById(@PathVariable("id") Long appraisalId) {
        return ResponseEntity.ok(employeeService.getMyAppraisalById(authenticatedUser.getId(), appraisalId));
    }

    @PostMapping("/appraisals/{id}/self-assessment")
    public ResponseEntity<AppraisalResponse> submitSelfAssessment(@PathVariable("id") Long appraisalId,
                                                                  @Valid @RequestBody SelfAssessmentRequest request) {
        return ResponseEntity.ok(employeeService.submitSelfAssessment(authenticatedUser.getId(), appraisalId, request));
    }

    @PatchMapping("/appraisals/{id}/acknowledge")
    public ResponseEntity<AppraisalResponse> acknowledgeAppraisal(@PathVariable("id") Long appraisalId) {
        return ResponseEntity.ok(employeeService.acknowledgeAppraisal(authenticatedUser.getId(), appraisalId));
    }

    @GetMapping("/goals")
    public ResponseEntity<List<GoalResponse>> getMyGoals() {
        return ResponseEntity.ok(employeeService.getMyGoals(authenticatedUser.getId()));
    }

    @PatchMapping("/goals/{id}/respond")
    public ResponseEntity<GoalResponse> respondToGoal(@PathVariable("id") Long goalId,
                                                      @RequestBody EmployeeGoalCompletionRequest request) {
        return ResponseEntity.ok(employeeService.respondToGoal(authenticatedUser.getId(), goalId, request));
    }

    @PutMapping("/appraisals/{id}/draft")
    public ResponseEntity<AppraisalResponse> saveSelfAssessmentDraft(
            @PathVariable("id") Long appraisalId,
            @RequestBody SelfAssessmentRequest request) {
        return ResponseEntity.ok(employeeService.saveSelfAssessmentDraft(authenticatedUser.getId(), appraisalId, request));
    }
}