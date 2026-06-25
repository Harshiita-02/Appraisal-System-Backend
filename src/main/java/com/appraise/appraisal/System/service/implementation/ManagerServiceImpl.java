package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.dtos.*;
import com.appraise.appraisal.System.entity.Appraisal;
import com.appraise.appraisal.System.entity.AppraisalCycle;
import com.appraise.appraisal.System.entity.Goal;
import com.appraise.appraisal.System.entity.User;
import com.appraise.appraisal.System.entity.enums.AppraisalStatus;
import com.appraise.appraisal.System.entity.enums.GoalEmployeeResponse;
import com.appraise.appraisal.System.entity.enums.GoalStatus;
import com.appraise.appraisal.System.exception.BadRequestException;
import com.appraise.appraisal.System.exception.ResourceNotFoundException;
import com.appraise.appraisal.System.mapper.AppraisalMapper;
import com.appraise.appraisal.System.mapper.GoalMapper;
import com.appraise.appraisal.System.repository.AppraisalCycleRepository;
import com.appraise.appraisal.System.repository.AppraisalRepository;
import com.appraise.appraisal.System.repository.GoalRepository;
import com.appraise.appraisal.System.repository.UserRepository;
import com.appraise.appraisal.System.service.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerServiceImpl implements ManagerService {

    private final AppraisalRepository appraisalRepository;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final AppraisalCycleRepository cycleRepository;
    private final GoalMapper goalMapper;

    @Override
    public ManagerDashboardResponse getDashboard(Long managerId) {
        List<Appraisal> teamAppraisals = appraisalRepository.findByManagerIdWithRelationships(managerId);
        List<Appraisal> myAppraisals = appraisalRepository.findByEmployeeIdWithRelationships(managerId);

        long teamSize = userRepository.findByManagerIdWithRelationships(managerId).size();
        long awaitingMyReview = teamAppraisals.stream()
                .filter(a -> a.getStatus() == AppraisalStatus.SELF_SUBMITTED)
                .count();
        long completed = teamAppraisals.stream()
                .filter(a -> a.getStatus() == AppraisalStatus.ACKNOWLEDGED)
                .count();

        ManagerDashboardSummary summary = new ManagerDashboardSummary(
                teamSize, teamAppraisals.size(), awaitingMyReview, completed);

        return new ManagerDashboardResponse(
                summary,
                myAppraisals.stream().map(AppraisalMapper::toResponse).toList(),
                teamAppraisals.stream().map(AppraisalMapper::toResponse).toList()
        );
    }

    @Override
    public List<TeamMemberResponse> getTeam(Long managerId) {
        return userRepository.findByManagerIdWithRelationships(managerId)
                .stream()
                .map(u -> new TeamMemberResponse(
                        u.getId(),
                        u.getName(),
                        u.getJobTitle(),
                        u.getDepartment() != null ? u.getDepartment().getName() : null,
                        u.getEmail(),
                        u.getStatus()
                ))
                .toList();
    }

    @Override
    public List<GoalResponse> getTeamGoals(Long managerId) {
        return appraisalRepository.findByManagerIdWithRelationships(managerId)
                .stream()
                .flatMap(a -> goalRepository.findByAppraisalIdWithRelationships(a.getId()).stream())
                .map(goalMapper::toResponse)
                .toList();
    }

    @Override
    public List<AppraisalResponse> getAssignableAppraisals(Long managerId) {
        return appraisalRepository.findByManagerIdWithRelationships(managerId)
                .stream()
                .map(AppraisalMapper::toResponse)
                .toList();
    }

    @Override
    public List<AppraisalResponse> getMyAppraisals(Long managerId) {
        return appraisalRepository.findByEmployeeIdWithRelationships(managerId)
                .stream()
                .map(AppraisalMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AppraisalResponse submitSelfAssessment(Long managerId, Long appraisalId, SelfAssessmentRequest request) {
        Appraisal appraisal = findOwnAppraisal(managerId, appraisalId);

        if (appraisal.getStatus() != AppraisalStatus.PENDING
                && appraisal.getStatus() != AppraisalStatus.EMPLOYEE_DRAFT) {
            throw new BadRequestException("This appraisal has already moved past the self-assessment stage.");
        }

        applySelfAssessmentFields(appraisal, request);
        appraisal.setStatus(AppraisalStatus.SELF_SUBMITTED);

        return AppraisalMapper.toResponse(appraisalRepository.save(appraisal));
    }

    @Override
    @Transactional
    public AppraisalResponse saveSelfAssessmentDraft(Long managerId, Long appraisalId, SelfAssessmentRequest request) {
        Appraisal appraisal = findOwnAppraisal(managerId, appraisalId);
        applySelfAssessmentFields(appraisal, request);
        appraisal.setStatus(AppraisalStatus.EMPLOYEE_DRAFT);
        return AppraisalMapper.toResponse(appraisalRepository.save(appraisal));
    }

    @Override
    public List<GoalResponse> getMyGoals(Long managerId) {
        return appraisalRepository.findByEmployeeIdWithRelationships(managerId)
                .stream()
                .flatMap(a -> goalRepository.findByAppraisalIdWithRelationships(a.getId()).stream())
                .map(goalMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public GoalResponse respondToGoal(Long managerId, Long goalId, EmployeeGoalCompletionRequest request) {
        Goal goal = goalRepository.findByIdWithRelationships(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId));

        if (goal.getUser() == null || !goal.getUser().getId().equals(managerId)) {
            throw new BadRequestException("This goal does not belong to the requesting manager");
        }

        if (request.getCompleted() == null) {
            // Clicked "Mark In Progress" — started working, no completion claim yet
            goal.setEmployeeResponse(GoalEmployeeResponse.IN_PROGRESS);
            goal.setStatus(GoalStatus.IN_PROGRESS);
        } else if (request.getCompleted()) {
            // Claims done — stays IN_PROGRESS until manager confirms
            goal.setEmployeeResponse(GoalEmployeeResponse.COMPLETED);
            goal.setStatus(GoalStatus.IN_PROGRESS);
        } else {
            // Claims not done — stays IN_PROGRESS until manager confirms
            goal.setEmployeeResponse(GoalEmployeeResponse.NOT_COMPLETED);
            goal.setStatus(GoalStatus.IN_PROGRESS);
        }

        if (request.getNote() != null) {
            goal.setEmployeeNote(request.getNote());
        }

        return goalMapper.toResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public GoalResponse createGoal(Long managerId, GoalRequest request) {
        Appraisal appraisal = appraisalRepository.findByIdWithRelationships(request.getAppraisalId())
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + request.getAppraisalId()));

        if (appraisal.getManager() == null || !appraisal.getManager().getId().equals(managerId)) {
            throw new BadRequestException("This appraisal does not belong to one of the requesting manager's reports");
        }

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

        return goalMapper.toResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public void deleteGoal(Long managerId, Long goalId) {
        Goal goal = goalRepository.findByIdWithRelationships(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId));

        if (goal.getAppraisal() == null
                || goal.getAppraisal().getManager() == null
                || !goal.getAppraisal().getManager().getId().equals(managerId)) {
            throw new BadRequestException("This goal does not belong to one of the requesting manager's reports");
        }

        goalRepository.delete(goal);
    }

    @Override
    @Transactional
    public GoalResponse confirmGoalStatus(Long managerId, Long goalId, boolean completed) {
        Goal goal = goalRepository.findByIdWithRelationships(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId));

        if (goal.getAppraisal() == null
                || goal.getAppraisal().getManager() == null
                || !goal.getAppraisal().getManager().getId().equals(managerId)) {
            throw new BadRequestException("This goal does not belong to one of the requesting manager's reports");
        }

        if (goal.getEmployeeResponse() == GoalEmployeeResponse.PENDING
                || goal.getEmployeeResponse() == GoalEmployeeResponse.IN_PROGRESS) {
            throw new BadRequestException(
                    "Cannot confirm this goal yet — the employee has not submitted a completion response.");
        }

        if (completed && goal.getEmployeeResponse() == GoalEmployeeResponse.NOT_COMPLETED) {
            throw new BadRequestException(
                    "Cannot mark as completed — the employee reported it as not done. " +
                            "Reset to Not Started if you want the employee to retry.");
        }

        goal.setStatus(completed ? GoalStatus.COMPLETED : GoalStatus.NOT_STARTED);

        return goalMapper.toResponse(goalRepository.save(goal));
    }

    @Override
    public TeamReportResponse getTeamReport(Long managerId, Long cycleId) {
        AppraisalCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal cycle not found with ID: " + cycleId));

        List<User> team = userRepository.findByManagerIdWithRelationships(managerId);

        List<TeamReportRow> rows = team.stream()
                .map(member -> buildReportRow(member, cycleId))
                .toList();

        List<Double> ratings = rows.stream()
                .map(TeamReportRow::getSelfRating)
                .filter(r -> r != null)
                .toList();

        Double avgRating = ratings.isEmpty()
                ? null
                : Math.round(ratings.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 10) / 10.0;

        return new TeamReportResponse(cycle.getName(), team.size(), avgRating, rows);
    }

    @Override
    @Transactional
    public AppraisalResponse reviewTeamAppraisal(Long managerId, Long appraisalId, ManagerReviewRequest request, boolean submit) {
        Appraisal appraisal = appraisalRepository.findByIdWithRelationships(appraisalId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + appraisalId));

        if (appraisal.getManager() == null || !appraisal.getManager().getId().equals(managerId)) {
            throw new BadRequestException("This appraisal does not belong to one of the requesting manager's reports");
        }

        if (appraisal.getStatus() != AppraisalStatus.SELF_SUBMITTED
                && appraisal.getStatus() != AppraisalStatus.MANAGER_DRAFT) {
            throw new BadRequestException(
                    "This appraisal isn't ready for manager review yet. Current status: " + appraisal.getStatus());
        }

        appraisal.setManagerRating(request.getManagerRating());
        appraisal.setManagerComments(request.getManagerComments());
        appraisal.setStatus(submit ? AppraisalStatus.MANAGER_REVIEWED : AppraisalStatus.MANAGER_DRAFT);


        return AppraisalMapper.toResponse(appraisalRepository.save(appraisal));
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private TeamReportRow buildReportRow(User member, Long cycleId) {
        Appraisal appraisal = appraisalRepository.findByEmployeeIdWithRelationships(member.getId())
                .stream()
                .filter(a -> a.getCycle() != null && a.getCycle().getId().equals(cycleId))
                .findFirst()
                .orElse(null);

        List<Goal> memberGoals = appraisal != null
                ? goalRepository.findByAppraisalIdWithRelationships(appraisal.getId())
                : List.of();

        long goalsCompleted = memberGoals.stream()
                .filter(g -> g.getStatus() == GoalStatus.COMPLETED)
                .count();

        return new TeamReportRow(
                appraisal != null ? appraisal.getId() : null,
                member.getId(),
                member.getName(),
                member.getJobTitle(),
                appraisal != null ? appraisal.getStatus() : AppraisalStatus.PENDING,
                appraisal != null ? appraisal.getSelfRating() : null,
                appraisal != null ? appraisal.getManagerRating() : null,
                goalsCompleted,
                memberGoals.size()
        );
    }

    private void applySelfAssessmentFields(Appraisal appraisal, SelfAssessmentRequest request) {
        appraisal.setWhatWentWell(request.getWhatWentWell());
        appraisal.setWhatToImprove(request.getWhatToImprove());
        appraisal.setKeyAchievements(request.getKeyAchievements());
        appraisal.setSelfRating(request.getSelfRating());
    }

    private Appraisal findOwnAppraisal(Long managerId, Long appraisalId) {
        Appraisal appraisal = appraisalRepository.findByIdWithRelationships(appraisalId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + appraisalId));

        if (appraisal.getEmployee() == null || !appraisal.getEmployee().getId().equals(managerId)) {
            throw new BadRequestException("This appraisal does not belong to the requesting manager");
        }

        return appraisal;
    }

    private void validateGoalTimeline(java.time.LocalDate dueDate, AppraisalCycle cycle) {
        if (dueDate == null) {
            throw new BadRequestException("Goal due date specification cannot be null");
        }
        if (dueDate.isBefore(java.time.LocalDate.now())) {
            throw new BadRequestException("Due date cannot be backdated to occur in the past");
        }
        if (cycle != null && cycle.getEndDate() != null && dueDate.isAfter(cycle.getEndDate())) {
            throw new BadRequestException("The due date (" + dueDate
                    + ") must fall within the appraisal cycle window (ends " + cycle.getEndDate() + ")");
        }
    }
}