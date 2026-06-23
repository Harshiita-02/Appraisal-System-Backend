package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.dtos.ReviewRequest;
import com.appraise.appraisal.System.dtos.ReviewResponse;
import com.appraise.appraisal.System.entity.Appraisal;
import com.appraise.appraisal.System.entity.Review;
import com.appraise.appraisal.System.entity.User;
import com.appraise.appraisal.System.entity.enums.NotificationType;
import com.appraise.appraisal.System.entity.enums.ReviewStatus;
import com.appraise.appraisal.System.exception.BadRequestException;
import com.appraise.appraisal.System.exception.DuplicateResourceException;
import com.appraise.appraisal.System.exception.ResourceNotFoundException;
import com.appraise.appraisal.System.mapper.ReviewMapper;
import com.appraise.appraisal.System.repository.AppraisalRepository;
import com.appraise.appraisal.System.repository.ReviewRepository;
import com.appraise.appraisal.System.repository.UserRepository;
import com.appraise.appraisal.System.service.NotificationService;
import com.appraise.appraisal.System.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppraisalRepository appraisalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        if (request == null) {
            throw new BadRequestException("Request payload cannot be null");
        }

        if (reviewRepository.existsByAppraisalIdAndManagerId(request.getAppraisalId(), request.getManagerId())) {
            throw new DuplicateResourceException("A review already exists from this manager for this appraisal");
        }

        Appraisal appraisal = appraisalRepository.findById(request.getAppraisalId())
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + request.getAppraisalId()));

        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with ID: " + request.getManagerId()));

        Review review = ReviewMapper.toEntity(request, appraisal, employee, manager);
        review.setStatus(ReviewStatus.DRAFT);

        Review saved = reviewRepository.save(review);

        // Notify employee that manager has started their review
        notificationService.createInternalNotification(
                employee,
                "Manager Review Started",
                manager.getName() + " has begun reviewing your appraisal. You will be notified once it is submitted.",
                NotificationType.REVIEW
        );

        return ReviewMapper.toResponse(saved);
    }

    @Override
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAllWithRelationships()
                .stream()
                .map(ReviewMapper::toResponse)
                .toList();
    }

    @Override
    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + id));
        return ReviewMapper.toResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        if (request == null) {
            throw new BadRequestException("Update payload cannot be null");
        }

        Review existingReview = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + id));

        if (existingReview.getStatus() == ReviewStatus.SUBMITTED) {
            throw new BadRequestException("This review has been submitted and cannot be modified");
        }

        ReviewStatus oldStatus = existingReview.getStatus();

        existingReview.setPerformanceRating(request.getPerformanceRating());
        existingReview.setComments(request.getComments().trim());
        existingReview.setStrengths(request.getStrengths() != null ? request.getStrengths().trim() : null);
        existingReview.setImprovements(request.getImprovements() != null ? request.getImprovements().trim() : null);

        if (request.getStatus() != null && !request.getStatus().trim().isBlank()) {
            try {
                existingReview.setStatus(ReviewStatus.valueOf(request.getStatus().trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid review status: " + request.getStatus());
            }
        }

        Review updated = reviewRepository.save(existingReview);

        // If newly submitted, notify the employee
        boolean justSubmitted = oldStatus == ReviewStatus.DRAFT
                && updated.getStatus() == ReviewStatus.SUBMITTED;

        if (justSubmitted && updated.getEmployee() != null) {
            notificationService.createInternalNotification(
                    updated.getEmployee(),
                    "Performance Review Submitted",
                    "Your manager has submitted your performance review. Check your appraisal for feedback and ratings.",
                    NotificationType.REVIEW
            );
        }

        return ReviewMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + id));

        if (review.getStatus() == ReviewStatus.SUBMITTED) {
            throw new BadRequestException("Submitted reviews cannot be deleted");
        }
        reviewRepository.delete(review);
    }
}
