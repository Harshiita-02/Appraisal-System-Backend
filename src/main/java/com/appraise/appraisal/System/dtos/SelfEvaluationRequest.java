package com.appraise.appraisal.System.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelfEvaluationRequest {

    @NotBlank(message = "Achievements record summary is required and cannot be empty")
    @Size(max = 2000, message = "Achievements note field description text must not exceed 2000 characters")
    private String achievements;

    @Size(max = 2000, message = "Challenges detail field text must not exceed 2000 characters")
    private String challenges;

    @Size(max = 2000, message = "Comments field text must not exceed 2000 characters")
    private String comments;

    @NotNull(message = "User ID reference binding assignment is required")
    private Long userId;

    @NotNull(message = "Appraisal Cycle ID reference link is required")
    private Long appraisalCycleId;
}