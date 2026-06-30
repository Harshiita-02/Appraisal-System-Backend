package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.dtos.*;
import com.appraise.appraisal.System.entity.*;
import com.appraise.appraisal.System.entity.enums.AppraisalStatus;
import com.appraise.appraisal.System.entity.enums.GoalEmployeeResponse;
import com.appraise.appraisal.System.entity.enums.GoalStatus;
import com.appraise.appraisal.System.entity.enums.NotificationType;
import com.appraise.appraisal.System.entity.enums.Roles;
import com.appraise.appraisal.System.exception.BadRequestException;
import com.appraise.appraisal.System.exception.ResourceNotFoundException;
import com.appraise.appraisal.System.mapper.AppraisalMapper;
import com.appraise.appraisal.System.mapper.GoalMapper;
import com.appraise.appraisal.System.repository.*;
import com.appraise.appraisal.System.service.HrService;
import com.appraise.appraisal.System.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HrServiceImpl implements HrService {

    private static final AppraisalStatus[] WORKFLOW_ORDER = {
            AppraisalStatus.PENDING,
            AppraisalStatus.EMPLOYEE_DRAFT,
            AppraisalStatus.SELF_SUBMITTED,
            AppraisalStatus.MANAGER_DRAFT,
            AppraisalStatus.MANAGER_REVIEWED,
            AppraisalStatus.APPROVED,
            AppraisalStatus.ACKNOWLEDGED
    };

    private final AppraisalRepository appraisalRepository;
    private final UserRepository userRepository;
    private final AppraisalCycleRepository cycleRepository;
    private final DepartmentRepository departmentRepository;
    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Override
    public DashboardResponse getDashboard() {
        List<Appraisal> all = appraisalRepository.findAllWithRelationships()
                .stream()
                .filter(a -> a.getEmployee() == null || a.getEmployee().getRole() != Roles.MANAGER)
                .toList();
        List<User> allUsers = userRepository.findAllWithRelationships();

        long activeEmployees = allUsers.stream()
                .filter(u -> u.getStatus() == com.appraise.appraisal.System.entity.enums.UserStatus.ACTIVE)
                .count();

        long pendingApproval = all.stream()
                .filter(a -> a.getStatus() == AppraisalStatus.MANAGER_REVIEWED)
                .count();

        long completed = all.stream()
                .filter(a -> a.getStatus() == AppraisalStatus.ACKNOWLEDGED)
                .count();

        DashboardSummary summary = new DashboardSummary(activeEmployees, all.size(), pendingApproval, completed);
        List<AppraisalResponse> appraisalResponses = all.stream().map(AppraisalMapper::toResponse).toList();

        return new DashboardResponse(summary, appraisalResponses);
    }

    @Override
    @Transactional
    public AppraisalResponse createSingleAppraisal(CreateAppraisalRequest request) {
        if (request.getEmployeeId() == null) {
            throw new BadRequestException("employeeId is required for single-mode appraisal creation");
        }

        User employee = userRepository.findByIdWithRelationships(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        if (appraisalRepository.existsByEmployeeIdAndCycleId(employee.getId(), request.getCycleId())) {
            throw new BadRequestException("This employee already has an appraisal for the selected cycle.");
        }

        Appraisal appraisal = buildAppraisalFor(employee, request.getCycleId());
        Appraisal saved = appraisalRepository.save(appraisal);
        notifyAppraisalCreated(saved);
        return AppraisalMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public List<AppraisalResponse> createAppraisalsForDepartment(CreateAppraisalRequest request) {
        if (request.getDepartmentId() == null) {
            throw new BadRequestException("departmentId is required for department-mode appraisal creation");
        }

        departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        List<User> targets = userRepository.findByDepartmentIdWithRelationships(request.getDepartmentId())
                .stream()
                .filter(u -> u.getRole() != Roles.HR && u.getRole() != Roles.MANAGER)
                .toList();

        return createForTargets(targets, request.getCycleId(), true);
    }

    @Override
    @Transactional
    public List<AppraisalResponse> createAppraisalsForAllEmployees(CreateAppraisalRequest request) {
        List<User> targets = userRepository.findAllWithRelationships()
                .stream()
                .filter(u -> u.getRole() != Roles.HR && u.getRole() != Roles.MANAGER)
                .toList();

        return createForTargets(targets, request.getCycleId(), true);
    }

    @Override
    @Transactional
    public AppraisalResponse advanceAppraisalStatus(Long appraisalId) {
        Appraisal appraisal = appraisalRepository.findByIdWithRelationships(appraisalId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + appraisalId));

        AppraisalStatus current = appraisal.getStatus();

        if (current != AppraisalStatus.MANAGER_REVIEWED && current != AppraisalStatus.APPROVED) {
            throw new BadRequestException("HR can only advance appraisals that are Manager Reviewed or Approved.");
        }

        int currentIndex = indexOf(current);
        AppraisalStatus next = WORKFLOW_ORDER[currentIndex + 1];
        appraisal.setStatus(next);
        Appraisal saved = appraisalRepository.save(appraisal);

        if (next == AppraisalStatus.APPROVED) {
            // Notify employee and manager — also sends emails
            if (saved.getEmployee() != null) {
                notificationService.createInternalNotification(
                        saved.getEmployee(),
                        "Appraisal Approved",
                        "Your appraisal for " + saved.getCycle().getName() + " has been approved by HR.",
                        NotificationType.SUCCESS
                );
            }
            if (saved.getManager() != null) {
                notificationService.createInternalNotification(
                        saved.getManager(),
                        "Appraisal Approved",
                        saved.getEmployee().getName() + "'s appraisal for " + saved.getCycle().getName()
                                + " has been approved by HR.",
                        NotificationType.SUCCESS
                );
            }
        } else if (next == AppraisalStatus.ACKNOWLEDGED) {
            if (saved.getEmployee() != null) {
                notificationService.createInternalNotification(
                        saved.getEmployee(),
                        "Appraisal Acknowledged",
                        "Your appraisal for " + saved.getCycle().getName()
                                + " has been marked as acknowledged by HR. This cycle is now complete.",
                        NotificationType.APPRAISAL
                );
            }
            if (saved.getManager() != null) {
                notificationService.createInternalNotification(
                        saved.getManager(),
                        "Appraisal Acknowledged",
                        saved.getEmployee().getName() + "'s appraisal for " + saved.getCycle().getName()
                                + " has been marked as acknowledged by HR. This cycle is now complete.",
                        NotificationType.APPRAISAL
                );
            }
        }

        return AppraisalMapper.toResponse(saved);
    }

    private void notifyAppraisalCreated(Appraisal appraisal) {
        if (appraisal.getEmployee() != null) {
            notificationService.createInternalNotification(
                    appraisal.getEmployee(),
                    "New Appraisal Created",
                    "A new appraisal for " + appraisal.getCycle().getName()
                            + " has been created for you. Fill out your self-assessment when ready.",
                    NotificationType.APPRAISAL
            );
        }
        if (appraisal.getManager() != null) {
            notificationService.createInternalNotification(
                    appraisal.getManager(),
                    "New Appraisal Created",
                    "A new appraisal for " + appraisal.getCycle().getName() + " has been created for "
                            + appraisal.getEmployee().getName() + ", one of your reports.",
                    NotificationType.APPRAISAL
            );
        }
    }

    @Override
    public CycleReportResponse getCycleReport(Long cycleId) {
        AppraisalCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal cycle not found with ID: " + cycleId));

        List<Appraisal> cycleAppraisals = appraisalRepository.findByCycleIdWithRelationships(cycleId);

        List<StatusBreakdownEntry> statusBreakdown = java.util.Arrays.stream(WORKFLOW_ORDER)
                .map(status -> new StatusBreakdownEntry(
                        status,
                        cycleAppraisals.stream().filter(a -> a.getStatus() == status).count()))
                .toList();

        List<Appraisal> rated = cycleAppraisals.stream()
                .filter(a -> a.getSelfRating() != null)
                .toList();

        List<RatingDistributionEntry> ratingDistribution = List.of(5, 4, 3, 2, 1).stream()
                .map(r -> new RatingDistributionEntry(
                        r,
                        rated.stream().filter(a -> Math.round(a.getSelfRating()) == r).count()))
                .toList();

        Double avgRating = rated.isEmpty()
                ? null
                : Math.round(rated.stream().mapToDouble(Appraisal::getSelfRating).average().orElse(0) * 10) / 10.0;

        long completedCount = cycleAppraisals.stream().filter(a -> a.getStatus() == AppraisalStatus.ACKNOWLEDGED).count();
        int completionPercent = cycleAppraisals.isEmpty()
                ? 0
                : (int) Math.round((completedCount * 100.0) / cycleAppraisals.size());
        long pendingActionCount = cycleAppraisals.stream().filter(a -> a.getStatus() != AppraisalStatus.ACKNOWLEDGED).count();

        Map<String, List<Appraisal>> byDept = cycleAppraisals.stream()
                .filter(a -> a.getEmployee() != null && a.getEmployee().getDepartment() != null)
                .collect(Collectors.groupingBy(a -> a.getEmployee().getDepartment().getName()));

        List<DepartmentReportRow> departmentRows = departmentRepository.findAll().stream()
                .map(dept -> {
                    List<Appraisal> deptAppraisals = byDept.getOrDefault(dept.getName(), List.of());
                    long deptCompleted = deptAppraisals.stream().filter(a -> a.getStatus() == AppraisalStatus.ACKNOWLEDGED).count();
                    List<Appraisal> deptRated = deptAppraisals.stream().filter(a -> a.getSelfRating() != null).toList();
                    Double deptAvg = deptRated.isEmpty()
                            ? null
                            : Math.round(deptRated.stream().mapToDouble(Appraisal::getSelfRating).average().orElse(0) * 10) / 10.0;
                    return new DepartmentReportRow(
                            dept.getName(),
                            dept.getUsers() != null ? dept.getUsers().size() : 0,
                            deptCompleted,
                            deptAppraisals.size() - deptCompleted,
                            deptAvg
                    );
                })
                .toList();

        List<PendingActionRow> pendingActions = cycleAppraisals.stream()
                .filter(a -> a.getStatus() != AppraisalStatus.ACKNOWLEDGED)
                .map(a -> new PendingActionRow(
                        a.getEmployee() != null ? a.getEmployee().getName() : null,
                        a.getEmployee() != null && a.getEmployee().getDepartment() != null
                                ? a.getEmployee().getDepartment().getName() : null,
                        a.getManager() != null ? a.getManager().getName() : null,
                        a.getStatus()
                ))
                .toList();

        return new CycleReportResponse(
                cycle.getName(),
                cycleAppraisals.size(),
                completionPercent,
                pendingActionCount,
                avgRating,
                statusBreakdown,
                ratingDistribution,
                departmentRows,
                pendingActions
        );
    }

    private List<AppraisalResponse> createForTargets(List<User> targets, Long cycleId, boolean throwIfEmpty) {
        cycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal cycle not found with ID: " + cycleId));

        List<Appraisal> created = new ArrayList<>();
        for (User employee : targets) {
            if (appraisalRepository.existsByEmployeeIdAndCycleId(employee.getId(), cycleId)) continue;
            created.add(buildAppraisalFor(employee, cycleId));
        }

        if (created.isEmpty() && throwIfEmpty) {
            throw new BadRequestException("Everyone in scope already has an appraisal for the selected cycle.");
        }

        List<Appraisal> saved = appraisalRepository.saveAll(created);
        saved.forEach(this::notifyAppraisalCreated);
        return saved.stream().map(AppraisalMapper::toResponse).toList();
    }

    private Appraisal buildAppraisalFor(User employee, Long cycleId) {
        AppraisalCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal cycle not found with ID: " + cycleId));

        if (employee.getManager() == null) {
            throw new BadRequestException(
                    "Employee '" + employee.getName() + "' has no assigned manager and cannot be given an appraisal yet");
        }

        Appraisal appraisal = new Appraisal();
        appraisal.setEmployee(employee);
        appraisal.setManager(employee.getManager());
        appraisal.setCycle(cycle);
        appraisal.setStatus(AppraisalStatus.PENDING);
        return appraisal;
    }

    private int indexOf(AppraisalStatus status) {
        for (int i = 0; i < WORKFLOW_ORDER.length; i++) {
            if (WORKFLOW_ORDER[i] == status) return i;
        }
        throw new BadRequestException("Unknown appraisal status: " + status);
    }

    @Override
    public List<GoalResponse> getAllGoals() {
        return goalRepository.findAllWithRelationships().stream().map(goalMapper::toResponse).toList();
    }

    @Override
    public List<AppraisalResponse> getAssignableAppraisals() {
        return appraisalRepository.findByEmployeeRoleWithRelationships(Roles.MANAGER)
                .stream().map(AppraisalMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public GoalResponse createGoal(GoalRequest request) {
        if (request == null) throw new BadRequestException("Goal request cannot be null");

        Appraisal appraisal = appraisalRepository.findByIdWithRelationships(request.getAppraisalId())
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + request.getAppraisalId()));

        if (appraisal.getEmployee().getRole() != Roles.MANAGER) {
            throw new BadRequestException("HR can only assign goals to managers.");
        }

        Goal goal = new Goal();
        goal.setTitle(request.getTitle().trim());
        goal.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        goal.setDueDate(request.getDueDate());
        goal.setUser(appraisal.getEmployee());
        goal.setAppraisalCycle(appraisal.getCycle());
        goal.setAppraisal(appraisal);
        goal.setStatus(GoalStatus.NOT_STARTED);
        goal.setEmployeeResponse(GoalEmployeeResponse.PENDING);

        return goalMapper.toResponse(goalRepository.save(goal));
    }

    @Override
    @Transactional
    public void deleteGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId));
        goalRepository.delete(goal);
    }

    @Override
    @Transactional
    public GoalResponse confirmGoalStatus(Long goalId, boolean completed) {
        Goal goal = goalRepository.findByIdWithRelationships(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId));

        if (goal.getEmployeeResponse() == GoalEmployeeResponse.PENDING
                || goal.getEmployeeResponse() == GoalEmployeeResponse.IN_PROGRESS) {
            throw new BadRequestException("Cannot confirm this goal yet — the manager has not submitted a completion response.");
        }

        if (completed && goal.getEmployeeResponse() == GoalEmployeeResponse.NOT_COMPLETED) {
            throw new BadRequestException("Cannot mark as completed — the manager reported it as not done.");
        }

        goal.setStatus(completed ? GoalStatus.COMPLETED : GoalStatus.NOT_STARTED);
        return goalMapper.toResponse(goalRepository.save(goal));
    }
}