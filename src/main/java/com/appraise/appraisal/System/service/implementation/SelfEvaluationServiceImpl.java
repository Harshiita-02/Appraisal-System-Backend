package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.dtos.SelfEvaluationRequest;
import com.appraise.appraisal.System.dtos.SelfEvaluationResponse;
import com.appraise.appraisal.System.entity.AppraisalCycle;
import com.appraise.appraisal.System.entity.SelfEvaluation;
import com.appraise.appraisal.System.entity.User;
import com.appraise.appraisal.System.entity.enums.NotificationType;
import com.appraise.appraisal.System.exception.BadRequestException;
import com.appraise.appraisal.System.exception.DuplicateResourceException;
import com.appraise.appraisal.System.exception.ResourceNotFoundException;
import com.appraise.appraisal.System.mapper.SelfEvaluationMapper;
import com.appraise.appraisal.System.repository.AppraisalCycleRepository;
import com.appraise.appraisal.System.repository.SelfEvaluationRepository;
import com.appraise.appraisal.System.repository.UserRepository;
import com.appraise.appraisal.System.service.NotificationService;
import com.appraise.appraisal.System.service.SelfEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SelfEvaluationServiceImpl implements SelfEvaluationService {

    private final SelfEvaluationRepository selfEvaluationRepository;
    private final AppraisalCycleRepository appraisalCycleRepository;
    private final UserRepository userRepository;
    private final SelfEvaluationMapper selfEvaluationMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public SelfEvaluationResponse createSelfEvaluation(SelfEvaluationRequest request) {
        if (request == null) {
            throw new BadRequestException("Self-evaluation request payload cannot be null");
        }

        // Guard: one self-evaluation per user per cycle
        if (selfEvaluationRepository.existsByUserIdAndAppraisalCycleId(request.getUserId(), request.getAppraisalCycleId())) {
            throw new DuplicateResourceException("A self-evaluation already exists for this user in this appraisal cycle");
        }

        User employee = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getUserId()));

        AppraisalCycle cycle = appraisalCycleRepository.findById(request.getAppraisalCycleId())
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal Cycle not found with ID: " + request.getAppraisalCycleId()));

        SelfEvaluation evaluation = new SelfEvaluation();
        evaluation.setAchievements(request.getAchievements().trim());
        evaluation.setChallenges(request.getChallenges() != null ? request.getChallenges().trim() : null);
        evaluation.setComments(request.getComments() != null ? request.getComments().trim() : null);
        evaluation.setUser(employee);
        evaluation.setAppraisalCycle(cycle);

        SelfEvaluation saved = selfEvaluationRepository.save(evaluation);

        // Confirm to employee
        notificationService.createInternalNotification(
                employee,
                "Self-Evaluation Saved",
                "Your self-evaluation for cycle '" + cycle.getName() + "' has been recorded successfully.",
                NotificationType.SUCCESS
        );

        return selfEvaluationMapper.toResponse(saved);
    }

    @Override
    public List<SelfEvaluationResponse> getAllSelfEvaluations() {
        return selfEvaluationRepository.findAllWithRelationships()
                .stream()
                .map(selfEvaluationMapper::toResponse)
                .toList();
    }

    @Override
    public SelfEvaluationResponse getSelfEvaluationById(Long id) {
        SelfEvaluation evaluation = selfEvaluationRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new ResourceNotFoundException("Self-evaluation not found with ID: " + id));
        return selfEvaluationMapper.toResponse(evaluation);
    }

    @Override
    @Transactional
    public SelfEvaluationResponse updateSelfEvaluation(Long id, SelfEvaluationRequest request) {
        if (request == null) {
            throw new BadRequestException("Update payload cannot be null");
        }

        SelfEvaluation existing = selfEvaluationRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new ResourceNotFoundException("Self-evaluation not found with ID: " + id));

        existing.setAchievements(request.getAchievements() != null ? request.getAchievements().trim() : existing.getAchievements());
        existing.setChallenges(request.getChallenges() != null ? request.getChallenges().trim() : existing.getChallenges());
        existing.setComments(request.getComments() != null ? request.getComments().trim() : existing.getComments());

        return selfEvaluationMapper.toResponse(selfEvaluationRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteSelfEvaluation(Long id) {
        SelfEvaluation evaluation = selfEvaluationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Self-evaluation not found with ID: " + id));
        selfEvaluationRepository.delete(evaluation);
    }
}
