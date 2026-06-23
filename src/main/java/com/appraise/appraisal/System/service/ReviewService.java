package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.ReviewRequest;
import com.appraise.appraisal.System.dtos.ReviewResponse;
import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request);
    List<ReviewResponse> getAllReviews();
    ReviewResponse getReviewById(Long id);
    ReviewResponse updateReview(Long id, ReviewRequest request);
    void deleteReview(Long id);
}