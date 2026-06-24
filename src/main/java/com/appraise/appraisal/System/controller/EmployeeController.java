package com.appraise.appraisal.System.controller;

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

/**
 * IMPORTANT — TEMPORARY employeeId QUERY PARAM:
 * Since security/JWT is intentionally not part of this pass, every method
 * below takes employeeId as a required query param so the API is callable
 * right now. When you add JWT auth: delete this parameter from each method
 * and read it from the SecurityContext instead. The service layer doesn't
 * need to change.
 */
@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/dashboard")
    public ResponseEntity<EmployeeDashboardResponse> getDashboard(@RequestParam Long employeeId) {
        return ResponseEntity.ok(employeeService.getDashboard(employeeId));
    }

    @GetMapping("/appraisals")
    public ResponseEntity<List<AppraisalResponse>> getMyAppraisals(@RequestParam Long employeeId) {
        return ResponseEntity.ok(employeeService.getMyAppraisals(employeeId));
    }

    @GetMapping("/appraisals/{id}")
    public ResponseEntity<AppraisalResponse> getMyAppraisalById(@RequestParam Long employeeId,
                                                                @PathVariable("id") Long appraisalId) {
        return ResponseEntity.ok(employeeService.getMyAppraisalById(employeeId, appraisalId));
    }

    @PostMapping("/appraisals/{id}/self-assessment")
    public ResponseEntity<AppraisalResponse> submitSelfAssessment(@RequestParam Long employeeId,
                                                                  @PathVariable("id") Long appraisalId,
                                                                  @Valid @RequestBody SelfAssessmentRequest request) {
        return ResponseEntity.ok(employeeService.submitSelfAssessment(employeeId, appraisalId, request));
    }

    @PatchMapping("/appraisals/{id}/acknowledge")
    public ResponseEntity<AppraisalResponse> acknowledgeAppraisal(@RequestParam Long employeeId,
                                                                  @PathVariable("id") Long appraisalId) {
        return ResponseEntity.ok(employeeService.acknowledgeAppraisal(employeeId, appraisalId));
    }

    @GetMapping("/goals")
    public ResponseEntity<List<GoalResponse>> getMyGoals(@RequestParam Long employeeId) {
        return ResponseEntity.ok(employeeService.getMyGoals(employeeId));
    }

    @PatchMapping("/goals/{id}/respond")
    public ResponseEntity<GoalResponse> respondToGoal(@RequestParam Long employeeId,
                                                      @PathVariable("id") Long goalId,
                                                      @RequestBody EmployeeGoalCompletionRequest request) {
        return ResponseEntity.ok(employeeService.respondToGoal(employeeId, goalId, request));
    }

    @PutMapping("/appraisals/{id}/draft")
    public ResponseEntity<AppraisalResponse> saveSelfAssessmentDraft(
            @RequestParam Long employeeId,
            @PathVariable("id") Long appraisalId,
            @RequestBody SelfAssessmentRequest request) {
        // Save without status transition — stays EMPLOYEE_DRAFT
        return ResponseEntity.ok(employeeService.saveSelfAssessmentDraft(employeeId, appraisalId, request));
    }
}