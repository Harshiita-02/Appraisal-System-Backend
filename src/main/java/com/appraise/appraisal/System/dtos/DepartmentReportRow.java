package com.appraise.appraisal.System.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentReportRow {
    private String department;
    private long employees;
    private long completed;
    private long pending;
    private Double avgRating;
}