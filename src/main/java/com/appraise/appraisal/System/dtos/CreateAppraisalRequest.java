package com.appraise.appraisal.System.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Covers all three modes the frontend's CreateAppraisalRequest union can
 * send: single (employeeId + cycleId), department (departmentId + cycleId),
 * all (cycleId only). The frontend calls three different routes for these
 * (see HrController) — mode itself isn't read off this DTO, the route path
 * is what selects the behavior.
 */
@Data
public class CreateAppraisalRequest {
    private String mode;

    private Long employeeId;
    private Long departmentId;

    @NotNull(message = "Cycle ID is required")
    private Long cycleId;
}