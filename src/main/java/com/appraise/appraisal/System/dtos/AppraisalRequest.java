package com.appraise.appraisal.System.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppraisalRequest {

    @NotNull(message = "EmployeeId is required")
    private Long employeeId;

    @NotNull(message = "ManagerId is required")
    private Long managerId;

    @NotNull(message = "CycleId is required")
    private Long cycleId;
}