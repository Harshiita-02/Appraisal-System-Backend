package com.appraise.appraisal.System.controller;

import com.appraise.appraisal.System.dtos.*;
import com.appraise.appraisal.System.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
public class HrController {

    private final HrService hrService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final AppraisalCycleService cycleService;
    private final AppraisalService appraisalService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(hrService.getDashboard());
    }

    // ---- Users -----------------------------------------------------------

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(userService.updateUserStatus(id, status));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Departments -----------------------------------------------------

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentResponse>> getDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @PostMapping("/departments")
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.createDepartment(request));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(@PathVariable Long id,
                                                               @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartmentById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Appraisals ------------------------------------------------------

    @GetMapping("/appraisals")
    public ResponseEntity<List<AppraisalResponse>> getAppraisals() {
        return ResponseEntity.ok(appraisalService.getAllAppraisals());
    }

    @PostMapping("/appraisals")
    public ResponseEntity<AppraisalResponse> createSingleAppraisal(@Valid @RequestBody CreateAppraisalRequest request) {
        return ResponseEntity.ok(hrService.createSingleAppraisal(request));
    }

    @PostMapping("/appraisals/bulk/department")
    public ResponseEntity<List<AppraisalResponse>> createAppraisalsForDepartment(
            @Valid @RequestBody CreateAppraisalRequest request) {
        return ResponseEntity.ok(hrService.createAppraisalsForDepartment(request));
    }

    @PostMapping("/appraisals/bulk/all")
    public ResponseEntity<List<AppraisalResponse>> createAppraisalsForAllEmployees(
            @Valid @RequestBody CreateAppraisalRequest request) {
        return ResponseEntity.ok(hrService.createAppraisalsForAllEmployees(request));
    }

    // IMPORTANT: /appraisals/assignable must be declared BEFORE /appraisals/{id}
    // so Spring matches the literal path "assignable" before treating it as an ID.
    @GetMapping("/appraisals/assignable")
    public ResponseEntity<List<AppraisalResponse>> getAssignableAppraisals() {
        return ResponseEntity.ok(hrService.getAssignableAppraisals());
    }

    @PatchMapping("/appraisals/{id}/status")
    public ResponseEntity<AppraisalResponse> advanceAppraisalStatus(@PathVariable Long id) {
        return ResponseEntity.ok(hrService.advanceAppraisalStatus(id));
    }

    @DeleteMapping("/appraisals/{id}")
    public ResponseEntity<Void> deleteAppraisal(@PathVariable Long id) {
        appraisalService.deleteAppraisal(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Cycles ----------------------------------------------------------

    @GetMapping("/cycles")
    public ResponseEntity<List<AppraisalCycleResponse>> getCycles() {
        return ResponseEntity.ok(cycleService.getAllCycles());
    }

    @PostMapping("/cycles")
    public ResponseEntity<AppraisalCycleResponse> createCycle(@Valid @RequestBody AppraisalCycleRequest request) {
        return ResponseEntity.ok(cycleService.createCycle(request));
    }

    // ---- Reports ---------------------------------------------------------

    @GetMapping("/reports")
    public ResponseEntity<CycleReportResponse> getCycleReport(@RequestParam Long cycleId) {
        return ResponseEntity.ok(hrService.getCycleReport(cycleId));
    }

    // ---- Goals -----------------------------------------------------------

    @GetMapping("/goals")
    public ResponseEntity<List<GoalResponse>> getGoals() {
        return ResponseEntity.ok(hrService.getAllGoals());
    }

    @PostMapping("/goals")
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        return ResponseEntity.ok(hrService.createGoal(request));
    }

    @DeleteMapping("/goals/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        hrService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/goals/{id}/confirm")
    public ResponseEntity<GoalResponse> confirmGoalStatus(@PathVariable Long id,
                                                          @RequestBody ConfirmGoalRequest request) {
        return ResponseEntity.ok(hrService.confirmGoalStatus(id, request.isCompleted()));
    }
}