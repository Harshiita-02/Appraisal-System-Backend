package com.appraise.appraisal.System.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private long activeEmployees;
    private long totalAppraisals;
    private long pendingApproval;
    private long completed;
}