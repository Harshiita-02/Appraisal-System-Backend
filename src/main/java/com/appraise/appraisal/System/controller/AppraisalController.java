package com.appraise.appraisal.System.controller;

import com.appraise.appraisal.System.dtos.AppraisalRequest;
import com.appraise.appraisal.System.dtos.AppraisalResponse;
import com.appraise.appraisal.System.entity.enums.AppraisalStatus;
import com.appraise.appraisal.System.exception.BadRequestException;
import com.appraise.appraisal.System.service.AppraisalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appraisals")
@RequiredArgsConstructor
public class AppraisalController {

    private final AppraisalService appraisalService;

    @PostMapping
    public ResponseEntity<AppraisalResponse> createAppraisal(@Valid @RequestBody AppraisalRequest request) {
        return new ResponseEntity<>(appraisalService.createAppraisal(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AppraisalResponse>> getAllAppraisals() {
        return ResponseEntity.ok(appraisalService.getAllAppraisals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppraisalResponse> getAppraisalById(@PathVariable Long id) {
        return ResponseEntity.ok(appraisalService.getAppraisalById(id));
    }

    @DeleteMapping("/appraisals/{id}")
    public ResponseEntity<Void> deleteAppraisal(@PathVariable Long id) {
        appraisalService.deleteAppraisal(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AppraisalResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        if (status == null || status.trim().isBlank()) {
            throw new BadRequestException("Status query parameter value string is required");
        }

        AppraisalStatus targetStatus;
        try {
            targetStatus = AppraisalStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid operational appraisal status value provided: '" + status + "'");
        }

        return ResponseEntity.ok(appraisalService.updateStatus(id, targetStatus));
    }


}