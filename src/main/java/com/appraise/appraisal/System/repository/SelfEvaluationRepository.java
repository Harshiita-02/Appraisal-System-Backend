package com.appraise.appraisal.System.repository;

import com.appraise.appraisal.System.entity.SelfEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SelfEvaluationRepository extends JpaRepository<SelfEvaluation, Long> {

    // Prevents an employee from creating multiple evaluation records for the same cycle
    boolean existsByUserIdAndAppraisalCycleId(Long userId, Long appraisalCycleId);

    @Query("SELECT s FROM SelfEvaluation s JOIN FETCH s.user JOIN FETCH s.appraisalCycle")
    List<SelfEvaluation> findAllWithRelationships();

    @Query("SELECT s FROM SelfEvaluation s JOIN FETCH s.user JOIN FETCH s.appraisalCycle WHERE s.id = :id")
    Optional<SelfEvaluation> findByIdWithRelationships(@Param("id") Long id);
}