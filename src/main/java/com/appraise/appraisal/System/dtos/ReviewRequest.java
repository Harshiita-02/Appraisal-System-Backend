package com.appraise.appraisal.System.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

        @NotNull(message = "Appraisal tracking ID reference is required")
        private Long appraisalId;

        @NotNull(message = "Target employee User ID reference is required")
        private Long employeeId;

        @NotNull(message = "Evaluating Manager User ID reference is required")
        private Long managerId;

        @NotNull(message = "Performance metric score rating is required")
        @Min(value = 1, message = "Performance rating must not fall below a baseline score of 1.0")
        @Max(value = 5, message = "Performance rating must not exceed a maximum corporate score boundary of 5.0")
        private Double performanceRating;

        @NotBlank(message = "Review feedback comments cannot be left empty")
        @Size(max = 1000, message = "Comments description field text must not exceed 1000 characters")
        private String comments;

        @Size(max = 1000, message = "Strengths core description text must not exceed 1000 characters")
        private String strengths;

        @Size(max = 1000, message = "Improvements core observation text must not exceed 1000 characters")
        private String improvements;

        private String status; // Enables safe status pipeline modification during updates
}