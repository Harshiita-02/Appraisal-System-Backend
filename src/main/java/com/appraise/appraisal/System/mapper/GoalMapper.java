package com.appraise.appraisal.System.mapper;

import com.appraise.appraisal.System.dtos.GoalResponse;
import com.appraise.appraisal.System.entity.Goal;
import org.springframework.stereotype.Component;

@Component
public class GoalMapper {

    public GoalResponse toResponse(Goal goal) {
        if (goal == null) {
            return null;
        }

        GoalResponse response = new GoalResponse();
        response.setId(goal.getId());
        response.setTitle(goal.getTitle());
        response.setDescription(goal.getDescription());
        response.setDueDate(goal.getDueDate());
        response.setStatus(goal.getStatus());
        response.setEmployeeResponse(goal.getEmployeeResponse());
        response.setEmployeeNote(goal.getEmployeeNote());
        response.setCreatedAt(goal.getCreatedAt());
        response.setUpdatedAt(goal.getUpdatedAt());

        if (goal.getAppraisal() != null) {
            response.setAppraisalId(goal.getAppraisal().getId());
        }

        if (goal.getUser() != null) {
            response.setEmployeeId(goal.getUser().getId());
            response.setEmployeeName(goal.getUser().getName());
        }

        if (goal.getAppraisalCycle() != null) {
            response.setCycle(goal.getAppraisalCycle().getName());
        }

        return response;
    }
}