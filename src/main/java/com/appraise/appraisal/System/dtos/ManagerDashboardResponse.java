package com.appraise.appraisal.System.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboardResponse {
    private ManagerDashboardSummary summary;
    private List<AppraisalResponse> myAppraisals;
    private List<AppraisalResponse> teamAppraisals;
}