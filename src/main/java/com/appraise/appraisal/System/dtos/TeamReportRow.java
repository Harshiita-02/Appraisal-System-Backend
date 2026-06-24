package com.appraise.appraisal.System.dtos;

import com.appraise.appraisal.System.entity.enums.AppraisalStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamReportRow {
    private Long appraisalId;
    private Long employeeId;
    private String employeeName;
    private String jobTitle;
    private AppraisalStatus status;
    private Double selfRating;
    private Double managerRating;
    private long goalsCompleted;
    private long goalsTotal;
}