package com.appraise.appraisal.System.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDashboardResponse {
    private long activeAppraisals;
    private long goalsInProgress;
    private long unreadNotifications;
    private List<AppraisalResponse> appraisals;
}