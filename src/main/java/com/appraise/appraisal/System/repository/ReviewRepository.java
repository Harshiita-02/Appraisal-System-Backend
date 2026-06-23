package com.appraise.appraisal.System.repository;

import com.appraise.appraisal.System.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Business Guard: Prevent a manager from creating multiple reviews for the same appraisal session
    boolean existsByAppraisalIdAndManagerId(Long appraisalId, Long managerId);

    @Query("SELECT r FROM Review r JOIN FETCH r.appraisal a JOIN FETCH a.cycle JOIN FETCH r.employee JOIN FETCH r.manager")
    List<Review> findAllWithRelationships();

    @Query("SELECT r FROM Review r JOIN FETCH r.appraisal a JOIN FETCH a.cycle JOIN FETCH r.employee JOIN FETCH r.manager WHERE r.id = :id")
    Optional<Review> findByIdWithRelationships(@Param("id") Long id);

    List<Review> findByAppraisalId(Long appraisalId);
    List<Review> findByEmployeeId(Long employeeId);
    List<Review> findByManagerId(Long managerId);
}