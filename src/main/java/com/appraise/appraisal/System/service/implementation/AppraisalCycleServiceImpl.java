
package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.dtos.AppraisalCycleRequest;
import com.appraise.appraisal.System.dtos.AppraisalCycleResponse;
import com.appraise.appraisal.System.entity.AppraisalCycle;
import com.appraise.appraisal.System.exception.BadRequestException;
import com.appraise.appraisal.System.exception.DuplicateResourceException;
import com.appraise.appraisal.System.exception.ResourceNotFoundException;
import com.appraise.appraisal.System.mapper.AppraisalCycleMapper;
import com.appraise.appraisal.System.repository.AppraisalCycleRepository;
import com.appraise.appraisal.System.service.AppraisalCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppraisalCycleServiceImpl implements AppraisalCycleService {

    private final AppraisalCycleRepository repository;
    private final AppraisalCycleMapper mapper;

    @Override
    @Transactional
    public AppraisalCycleResponse createCycle(AppraisalCycleRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body cannot be null");
        }

        validateDates(request);

        String normalizedName = request.getName().trim();
        if (repository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourceException("Appraisal cycle with name '" + normalizedName + "' already exists");
        }

        // Business Rule Guard Rail: Enforce exactly one active cycle at a time across the enterprise
        if (request.getActive() != null && request.getActive()) {
            repository.deactivateAllActiveCycles();
        }

        AppraisalCycle cycle = mapper.toEntity(request);
        cycle.setName(normalizedName);

        return mapper.toResponse(repository.save(cycle));
    }

    @Override
    public List<AppraisalCycleResponse> getAllCycles() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public AppraisalCycleResponse getCycleById(Long id) {
        AppraisalCycle cycle = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal cycle not found with ID: " + id));
        return mapper.toResponse(cycle);
    }

    @Override
    @Transactional
    public AppraisalCycleResponse updateCycle(Long id, AppraisalCycleRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body cannot be null");
        }

        validateDates(request);

        AppraisalCycle cycle = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal cycle not found with ID: " + id));

        String normalizedName = request.getName().trim();
        if (!cycle.getName().equalsIgnoreCase(normalizedName) && repository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourceException("Appraisal cycle with name '" + normalizedName + "' already exists");
        }

        // Manage active cycle overrides safely
        if (request.getActive() != null && request.getActive() && !cycle.getActive()) {
            repository.deactivateAllActiveCycles();
        }

        cycle.setName(normalizedName);
        cycle.setStartDate(request.getStartDate());
        cycle.setEndDate(request.getEndDate());
        cycle.setActive(request.getActive() != null ? request.getActive() : cycle.getActive());

        return mapper.toResponse(repository.save(cycle));
    }

    @Override
    @Transactional
    public void deleteCycle(Long id) {
        AppraisalCycle cycle = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal cycle not found with ID: " + id));

        // Safeguard: Stop cascading database errors if evaluations have already started within this timeline
        if (cycle.getAppraisals() != null && !cycle.getAppraisals().isEmpty()) {
            throw new BadRequestException("Cannot delete this appraisal cycle because it contains active employee evaluations logs");
        }

        repository.delete(cycle);
    }

    private void validateDates(AppraisalCycleRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BadRequestException("Start date and end date must not be null");
        }
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new BadRequestException("Invalid Timeline: The cycle End Date must strictly be configured to occur after its Start Date");
        }
    }
}