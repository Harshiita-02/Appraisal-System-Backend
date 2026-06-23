package com.appraise.appraisal.System.mapper;

import com.appraise.appraisal.System.dtos.ReviewRequest;
import com.appraise.appraisal.System.dtos.ReviewResponse;
import com.appraise.appraisal.System.entity.Appraisal;
import com.appraise.appraisal.System.entity.Review;
import com.appraise.appraisal.System.entity.User;

public class ReviewMapper {

    public static Review toEntity(ReviewRequest request, Appraisal appraisal, User employee, User manager) {
        if (request == null) return null;

        Review review = new Review();
        review.setAppraisal(appraisal);
        review.setEmployee(employee);
        review.setManager(manager);
        review.setPerformanceRating(request.getPerformanceRating());
        review.setComments(request.getComments() != null ? request.getComments().trim() : null);
        review.setStrengths(request.getStrengths() != null ? request.getStrengths().trim() : null);
        review.setImprovements(request.getImprovements() != null ? request.getImprovements().trim() : null);
        return review;
    }

    public static ReviewResponse toResponse(Review review) {
        if (review == null) return null;

        // Safely extract the cycle identifier link to preserve data tracing rules
        Long extractedCycleId = (review.getAppraisal() != null && review.getAppraisal().getCycle() != null)
                ? review.getAppraisal().getCycle().getId()
                : null;

        return new ReviewResponse(
                review.getId(),
                review.getAppraisal() != null ? review.getAppraisal().getId() : null,
                review.getEmployee() != null ? review.getEmployee().getId() : null,
                review.getManager() != null ? review.getManager().getId() : null,
                extractedCycleId, // Successfully resolved compilation constructor mismatch here
                review.getPerformanceRating(),
                review.getComments(),
                review.getStrengths(),
                review.getImprovements(),
                review.getStatus(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}