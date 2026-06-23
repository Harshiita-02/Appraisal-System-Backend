package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.dtos.GoalRequest;
import com.appraise.appraisal.System.dtos.GoalResponse;
import com.appraise.appraisal.System.entity.Appraisal;
import com.appraise.appraisal.System.entity.AppraisalCycle;
import com.appraise.appraisal.System.entity.Goal;
import com.appraise.appraisal.System.entity.enums.GoalEmployeeResponse;
import com.appraise.appraisal.System.entity.enums.GoalStatus;
import com.appraise.appraisal.System.exception.BadRequestException;
import com.appraise.appraisal.System.exception.ResourceNotFoundException;
import com.appraise.appraisal.System.mapper.GoalMapper;
import com.appraise.appraisal.System.repository.AppraisalRepository;
import com.appraise.appraisal.System.repository.GoalRepository;
import com.appraise.appraisal.System.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final AppraisalRepository appraisalRepository;
    private final GoalMapper goalMapper;

    @Override
    @Transactional
    public GoalResponse createGoal(GoalRequest request) {
        if (request == null) {
            throw new BadRequestException("Goal request payload body cannot be null");
        }

        Appraisal appraisal = appraisalRepository.findByIdWithRelationships(request.getAppraisalId())
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + request.getAppraisalId()));

        AppraisalCycle cycle = appraisal.getCycle();

        validateGoalTimeline(request.getDueDate(), cycle);

        Goal goal = new Goal();
        goal.setTitle(request.getTitle().trim());
        goal.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        goal.setDueDate(request.getDueDate());
        goal.setUser(appraisal.getEmployee());
        goal.setAppraisalCycle(cycle);
        goal.setAppraisal(appraisal);
        goal.setStatus(GoalStatus.NOT_STARTED);
        goal.setEmployeeResponse(GoalEmployeeResponse.PENDING);

        Goal savedGoal = goalRepository.save(goal);
        return goalMapper.toResponse(savedGoal);
    }

    @Override
    public List<GoalResponse> getAllGoals() {
        return goalRepository.findAllWithRelationships()
                .stream()
                .map(goalMapper::toResponse)
                .toList();
    }

    @Override
    public GoalResponse getGoalById(Long id) {
        Goal goal = goalRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal metric file not found with ID: " + id));
        return goalMapper.toResponse(goal);
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(Long id, GoalRequest request) {
        if (request == null) {
            throw new BadRequestException("Goal update request payload body cannot be null");
        }

        Goal existingGoal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal metric file not found with ID: " + id));

        if (request.getAppraisalId() != null
                && (existingGoal.getAppraisal() == null
                || !request.getAppraisalId().equals(existingGoal.getAppraisal().getId()))) {
            Appraisal appraisal = appraisalRepository.findByIdWithRelationships(request.getAppraisalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + request.getAppraisalId()));
            existingGoal.setAppraisal(appraisal);
            existingGoal.setUser(appraisal.getEmployee());
            existingGoal.setAppraisalCycle(appraisal.getCycle());
        }

        if (request.getDueDate() != null) {
            validateGoalTimeline(request.getDueDate(), existingGoal.getAppraisalCycle());
            existingGoal.setDueDate(request.getDueDate());
        }

        if (request.getTitle() != null) {
            existingGoal.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            existingGoal.setDescription(request.getDescription().trim());
        }

        if (request.getStatus() != null && !request.getStatus().trim().isBlank()) {
            try {
                existingGoal.setStatus(GoalStatus.valueOf(request.getStatus().trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid standard corporate goal status tracking value provided: " + request.getStatus());
            }
        }

        if (request.getEmployeeResponse() != null && !request.getEmployeeResponse().trim().isBlank()) {
            try {
                existingGoal.setEmployeeResponse(GoalEmployeeResponse.valueOf(request.getEmployeeResponse().trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid employee response value provided: " + request.getEmployeeResponse());
            }
        }
        if (request.getEmployeeNote() != null) {
            existingGoal.setEmployeeNote(request.getEmployeeNote().trim());
        }

        Goal updatedGoal = goalRepository.save(existingGoal);
        return goalMapper.toResponse(updatedGoal);
    }

    @Override
    @Transactional
    public void deleteGoal(Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal metric file not found with ID: " + id));
        goalRepository.delete(goal);
    }

    private void validateGoalTimeline(LocalDate dueDate, AppraisalCycle cycle) {
        if (dueDate == null) {
            throw new BadRequestException("Goal due date specification cannot be null");
        }
        if (dueDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Invalid Configuration: Due date cannot be backdated to occur in the past");
        }
        if (cycle != null && cycle.getEndDate() != null && dueDate.isAfter(cycle.getEndDate())) {
            throw new BadRequestException("Timeline Boundary Violation: The designated due date (" + dueDate
                    + ") must fall within the absolute boundaries of the tracking Appraisal Cycle window (" + cycle.getEndDate() + ")");
        }
    }
}