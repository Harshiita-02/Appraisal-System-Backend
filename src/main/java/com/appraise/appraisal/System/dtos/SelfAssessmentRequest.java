package com.appraise.appraisal.System.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelfAssessmentRequest {
    private String whatWentWell;
    private String whatToImprove;
    private String keyAchievements;

    @NotNull(message = "Self rating is required")
    private Double selfRating;
}