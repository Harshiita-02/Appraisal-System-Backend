package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.dtos.AppraisalRequest;
import com.appraise.appraisal.System.dtos.AppraisalResponse;
import com.appraise.appraisal.System.entity.Appraisal;
import com.appraise.appraisal.System.entity.AppraisalCycle;
import com.appraise.appraisal.System.entity.User;
import com.appraise.appraisal.System.entity.enums.AppraisalStatus;
import com.appraise.appraisal.System.exception.BadRequestException;
import com.appraise.appraisal.System.exception.DuplicateResourceException;
import com.appraise.appraisal.System.entity.enums.Roles;
import com.appraise.appraisal.System.exception.ResourceNotFoundException;
import com.appraise.appraisal.System.mapper.AppraisalMapper;
import com.appraise.appraisal.System.repository.AppraisalCycleRepository;
import com.appraise.appraisal.System.repository.AppraisalRepository;
import com.appraise.appraisal.System.repository.UserRepository;
import com.appraise.appraisal.System.service.AppraisalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppraisalServiceImpl implements AppraisalService {

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

    @Override
    @Transactional
    public AppraisalResponse createAppraisal(AppraisalRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body cannot be null");
        }

        if (appraisalRepository.existsByEmployeeIdAndCycleId(request.getEmployeeId(), request.getCycleId())) {
            throw new DuplicateResourceException("An active appraisal record already exists for this employee in this cycle");
        }

        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.getManagerId()));

        AppraisalCycle cycle = cycleRepository.findById(request.getCycleId())
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal Cycle not found with ID: " + request.getCycleId()));

        if (cycle.getActive() != null && !cycle.getActive()) {
            throw new BadRequestException("Cannot create appraisal logs under an inactive or closed appraisal cycle");
        }

        Appraisal appraisal = new Appraisal();
        appraisal.setEmployee(employee);
        appraisal.setManager(manager);
        appraisal.setCycle(cycle);
        appraisal.setStatus(AppraisalStatus.PENDING);

        Appraisal saved = appraisalRepository.save(appraisal);
        return AppraisalMapper.toResponse(saved);
    }

    @Override
    public List<AppraisalResponse> getAllAppraisals() {
        return appraisalRepository.findAllWithRelationships()
                .stream()
                .filter(a -> a.getEmployee() == null || a.getEmployee().getRole() != Roles.MANAGER)
                .map(AppraisalMapper::toResponse)
                .toList();
    }

    @Override
    public AppraisalResponse getAppraisalById(Long id) {
        Appraisal appraisal = appraisalRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal file records not found with ID: " + id));
        return AppraisalMapper.toResponse(appraisal);
    }

    @Override
    @Transactional
    public void deleteAppraisal(Long id) {
        Appraisal appraisal = appraisalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal file records not found with ID: " + id));

        if (appraisal.getStatus() == AppraisalStatus.ACKNOWLEDGED) {
            throw new BadRequestException("Archived and Acknowledged appraisals cannot be deleted from the corporate database system");
        }

        appraisalRepository.delete(appraisal);
    }

    @Override
    @Transactional
    public AppraisalResponse updateStatus(Long id, AppraisalStatus newStatus) {
        Appraisal appraisal = appraisalRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal records not found with ID: " + id));

        validateStatusTransition(appraisal.getStatus(), newStatus);

        appraisal.setStatus(newStatus);
        Appraisal updated = appraisalRepository.save(appraisal);
        return AppraisalMapper.toResponse(updated);
    }

    private void validateStatusTransition(AppraisalStatus currentStatus, AppraisalStatus newStatus) {
        if (currentStatus == null || newStatus == null) {
            throw new BadRequestException("Appraisal processing state status values cannot be null");
        }

        if (currentStatus == newStatus) {
            return;
        }

        int currentIndex = indexOf(currentStatus);
        int newIndex = indexOf(newStatus);

        if (currentIndex == WORKFLOW_ORDER.length - 1) {
            throw new BadRequestException(
                    "Workflow Locked: This file tracking record has achieved " + currentStatus +
                            " closure status and cannot be modified");
        }

        if (newIndex != currentIndex + 1) {
            throw new BadRequestException(
                    "Invalid Action Sequence: " + currentStatus + " can only advance to " +
                            WORKFLOW_ORDER[currentIndex + 1] + ", not " + newStatus);
        }
    }

    private int indexOf(AppraisalStatus status) {
        for (int i = 0; i < WORKFLOW_ORDER.length; i++) {
            if (WORKFLOW_ORDER[i] == status) return i;
        }
        throw new BadRequestException("Unknown appraisal status: " + status);
    }
}