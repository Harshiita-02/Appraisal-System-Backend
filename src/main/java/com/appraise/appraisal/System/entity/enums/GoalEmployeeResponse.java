package com.appraise.appraisal.System.entity.enums;

public enum GoalEmployeeResponse {
    PENDING,        // goal assigned, employee hasn't acted yet
    IN_PROGRESS,    // employee acknowledged and started working
    COMPLETED,      // employee claims done — awaiting manager confirmation
    NOT_COMPLETED   // employee claims not done — awaiting manager confirmation
}