package com.appraise.appraisal.System.dtos;

import com.appraise.appraisal.System.entity.enums.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long appraisalId;
    private Long employeeId;
    private Long managerId;
    private Long cycleId; // Maintained model integrity parameters

    private Double performanceRating;
    private String comments;
    private String strengths;
    private String improvements;

    private ReviewStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}