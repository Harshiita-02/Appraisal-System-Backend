package com.appraise.appraisal.System.dtos;

import lombok.Data;

@Data
public class EmployeeGoalCompletionRequest {
    // null = mark as in-progress only (no completion claim yet)
    // true = employee claims completed
    // false = employee claims not completed
    private Boolean completed;  // changed from primitive boolean to Boolean (nullable)
    private String note;
}