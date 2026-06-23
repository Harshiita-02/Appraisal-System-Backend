package com.appraise.appraisal.System.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboardSummary {
    private long teamSize;
    private long activeReviews;
    private long awaitingMyReview;
    private long completed;
}