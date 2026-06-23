package com.appraise.appraisal.System.dtos;

import lombok.Data;

@Data
public class EmployeeGoalCompletionRequest {
    private boolean completed;
    private String note;
}