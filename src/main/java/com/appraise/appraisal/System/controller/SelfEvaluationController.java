package com.appraise.appraisal.System.controller;

import com.appraise.appraisal.System.dtos.SelfEvaluationRequest;
import com.appraise.appraisal.System.dtos.SelfEvaluationResponse;
import com.appraise.appraisal.System.service.SelfEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/self-evaluations")
@RequiredArgsConstructor
public class SelfEvaluationController {

    private final SelfEvaluationService selfEvaluationService;

    @PostMapping
    public ResponseEntity<SelfEvaluationResponse> createSelfEvaluation(@Valid @RequestBody SelfEvaluationRequest request) {
        return new ResponseEntity<>(selfEvaluationService.createSelfEvaluation(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SelfEvaluationResponse>> getAllSelfEvaluations() {
        return ResponseEntity.ok(selfEvaluationService.getAllSelfEvaluations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SelfEvaluationResponse> getSelfEvaluationById(@PathVariable Long id) {
        return ResponseEntity.ok(selfEvaluationService.getSelfEvaluationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SelfEvaluationResponse> updateSelfEvaluation(
            @PathVariable Long id,
            @Valid @RequestBody SelfEvaluationRequest request) {
        return ResponseEntity.ok(selfEvaluationService.updateSelfEvaluation(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSelfEvaluation(@PathVariable Long id) {
        selfEvaluationService.deleteSelfEvaluation(id);
        return ResponseEntity.ok("Self evaluation tracking record deleted successfully");
    }
}