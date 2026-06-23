package com.appraise.appraisal.System.repository;

import com.appraise.appraisal.System.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    @Query("SELECT g FROM Goal g JOIN FETCH g.user JOIN FETCH g.appraisalCycle")
    List<Goal> findAllWithRelationships();

    @Query("SELECT g FROM Goal g JOIN FETCH g.user JOIN FETCH g.appraisalCycle WHERE g.id = :id")
    Optional<Goal> findByIdWithRelationships(@Param("id") Long id);

    List<Goal> findByUserId(Long userId);

    @Query("SELECT g FROM Goal g JOIN FETCH g.user JOIN FETCH g.appraisalCycle WHERE g.appraisal.id = :appraisalId")
    List<Goal> findByAppraisalIdWithRelationships(@Param("appraisalId") Long appraisalId);
}