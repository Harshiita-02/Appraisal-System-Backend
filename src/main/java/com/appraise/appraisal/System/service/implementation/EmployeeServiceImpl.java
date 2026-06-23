package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.dtos.AppraisalResponse;
import com.appraise.appraisal.System.dtos.EmployeeDashboardResponse;
import com.appraise.appraisal.System.dtos.EmployeeGoalCompletionRequest;
import com.appraise.appraisal.System.dtos.GoalResponse;
import com.appraise.appraisal.System.dtos.SelfAssessmentRequest;
import com.appraise.appraisal.System.entity.Appraisal;
import com.appraise.appraisal.System.entity.Goal;
import com.appraise.appraisal.System.entity.enums.AppraisalStatus;
import com.appraise.appraisal.System.entity.enums.GoalEmployeeResponse;
import com.appraise.appraisal.System.entity.enums.GoalStatus;
import com.appraise.appraisal.System.exception.BadRequestException;
import com.appraise.appraisal.System.exception.ResourceNotFoundException;
import com.appraise.appraisal.System.mapper.AppraisalMapper;
import com.appraise.appraisal.System.mapper.GoalMapper;
import com.appraise.appraisal.System.repository.AppraisalRepository;
import com.appraise.appraisal.System.repository.GoalRepository;
import com.appraise.appraisal.System.repository.NotificationRepository;
import com.appraise.appraisal.System.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final AppraisalRepository appraisalRepository;
    private final GoalRepository goalRepository;
    private final NotificationRepository notificationRepository;
    private final GoalMapper goalMapper;

    @Override
    public EmployeeDashboardResponse getDashboard(Long employeeId) {
        List<Appraisal> appraisals = appraisalRepository.findByEmployeeIdWithRelationships(employeeId);

        long activeAppraisals = appraisals.stream()
                .filter(a -> a.getStatus() != AppraisalStatus.ACKNOWLEDGED)
                .count();

        long goalsInProgress = appraisals.stream()
                .flatMap(a -> goalRepository.findByAppraisalIdWithRelationships(a.getId()).stream())
                .filter(g -> g.getStatus() == GoalStatus.IN_PROGRESS)
                .count();

        long unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseWithRelationships(employeeId)
                .size();

        List<AppraisalResponse> appraisalResponses = appraisals.stream()
                .map(AppraisalMapper::toResponse)
                .toList();

        return new EmployeeDashboardResponse(activeAppraisals, goalsInProgress, unreadNotifications, appraisalResponses);
    }

    @Override
    public List<AppraisalResponse> getMyAppraisals(Long employeeId) {
        return appraisalRepository.findByEmployeeIdWithRelationships(employeeId)
                .stream()
                .map(AppraisalMapper::toResponse)
                .toList();
    }

    @Override
    public AppraisalResponse getMyAppraisalById(Long employeeId, Long appraisalId) {
        Appraisal appraisal = findOwnedAppraisal(employeeId, appraisalId);
        return AppraisalMapper.toResponse(appraisal);
    }

    @Override
    @Transactional
    public AppraisalResponse submitSelfAssessment(Long employeeId, Long appraisalId, SelfAssessmentRequest request) {
        Appraisal appraisal = findOwnedAppraisal(employeeId, appraisalId);

        if (appraisal.getStatus() != AppraisalStatus.PENDING
                && appraisal.getStatus() != AppraisalStatus.EMPLOYEE_DRAFT) {
            throw new BadRequestException(
                    "Self-assessment can only be submitted while the appraisal is PENDING or EMPLOYEE_DRAFT, not "
                            + appraisal.getStatus());
        }

        appraisal.setWhatWentWell(request.getWhatWentWell());
        appraisal.setWhatToImprove(request.getWhatToImprove());
        appraisal.setKeyAchievements(request.getKeyAchievements());
        appraisal.setSelfRating(request.getSelfRating());
        appraisal.setStatus(AppraisalStatus.SELF_SUBMITTED);

        Appraisal saved = appraisalRepository.save(appraisal);
        return AppraisalMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AppraisalResponse acknowledgeAppraisal(Long employeeId, Long appraisalId) {
        Appraisal appraisal = findOwnedAppraisal(employeeId, appraisalId);

        if (appraisal.getStatus() != AppraisalStatus.APPROVED) {
            throw new BadRequestException(
                    "This appraisal is not ready to be acknowledged yet. Current status: " + appraisal.getStatus());
        }

        appraisal.setStatus(AppraisalStatus.ACKNOWLEDGED);
        Appraisal saved = appraisalRepository.save(appraisal);
        return AppraisalMapper.toResponse(saved);
    }

    @Override
    public List<GoalResponse> getMyGoals(Long employeeId) {
        return appraisalRepository.findByEmployeeIdWithRelationships(employeeId)
                .stream()
                .flatMap(a -> goalRepository.findByAppraisalIdWithRelationships(a.getId()).stream())
                .map(goalMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public GoalResponse respondToGoal(Long employeeId, Long goalId, EmployeeGoalCompletionRequest request) {
        Goal goal = goalRepository.findByIdWithRelationships(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId));

        if (goal.getUser() == null || !goal.getUser().getId().equals(employeeId)) {
            throw new BadRequestException("This goal does not belong to the requesting employee");
        }

        goal.setEmployeeResponse(request.isCompleted()
                ? GoalEmployeeResponse.COMPLETED
                : GoalEmployeeResponse.NOT_COMPLETED);
        goal.setEmployeeNote(request.getNote());

        Goal saved = goalRepository.save(goal);
        return goalMapper.toResponse(saved);
    }

    private Appraisal findOwnedAppraisal(Long employeeId, Long appraisalId) {
        Appraisal appraisal = appraisalRepository.findByIdWithRelationships(appraisalId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + appraisalId));

        if (appraisal.getEmployee() == null || !appraisal.getEmployee().getId().equals(employeeId)) {
            throw new BadRequestException("This appraisal does not belong to the requesting employee");
        }

        return appraisal;
    }
}