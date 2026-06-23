package com.appraise.appraisal.System.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleReportResponse {
    private String cycle;
    private long totalAppraisals;
    private int completionPercent;
    private long pendingActionCount;
    private Double avgRating;
    private List<StatusBreakdownEntry> statusBreakdown;
    private List<RatingDistributionEntry> ratingDistribution;
    private List<DepartmentReportRow> byDepartment;
    private List<PendingActionRow> pendingActions;
}