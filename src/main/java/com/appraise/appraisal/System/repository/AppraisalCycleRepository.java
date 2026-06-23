package com.appraise.appraisal.System.repository;

import com.appraise.appraisal.System.entity.AppraisalCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppraisalCycleRepository extends JpaRepository<AppraisalCycle, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<AppraisalCycle> findByActiveTrue();

    // High performance bulk modification query to deactivate all active cycles instantly
    @Modifying
    @Query("UPDATE AppraisalCycle c SET c.active = false WHERE c.active = true")
    void deactivateAllActiveCycles();
}