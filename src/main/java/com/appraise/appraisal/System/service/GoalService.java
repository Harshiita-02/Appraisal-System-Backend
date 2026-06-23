package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.GoalRequest;
import com.appraise.appraisal.System.dtos.GoalResponse;
import java.util.List;

public interface GoalService {
    GoalResponse createGoal(GoalRequest request);
    List<GoalResponse> getAllGoals();
    GoalResponse getGoalById(Long id);
    GoalResponse updateGoal(Long id, GoalRequest request);
    void deleteGoal(Long id);
}