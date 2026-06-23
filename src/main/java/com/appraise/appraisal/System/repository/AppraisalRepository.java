package com.appraise.appraisal.System.repository;

import com.appraise.appraisal.System.entity.Appraisal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalRepository extends JpaRepository<Appraisal, Long> {

    boolean existsByEmployeeIdAndCycleId(Long employeeId, Long cycleId);

    @Query("SELECT a FROM Appraisal a JOIN FETCH a.employee JOIN FETCH a.manager JOIN FETCH a.cycle")
    List<Appraisal> findAllWithRelationships();

    @Query("SELECT a FROM Appraisal a JOIN FETCH a.employee JOIN FETCH a.manager JOIN FETCH a.cycle WHERE a.id = :id")
    Optional<Appraisal> findByIdWithRelationships(@Param("id") Long id);

    List<Appraisal> findByEmployeeId(Long employeeId);
    List<Appraisal> findByCycleId(Long cycleId);

    @Query("SELECT a FROM Appraisal a JOIN FETCH a.employee JOIN FETCH a.manager JOIN FETCH a.cycle WHERE a.employee.id = :employeeId")
    List<Appraisal> findByEmployeeIdWithRelationships(@Param("employeeId") Long employeeId);

    @Query("SELECT a FROM Appraisal a JOIN FETCH a.employee JOIN FETCH a.manager JOIN FETCH a.cycle WHERE a.manager.id = :managerId")
    List<Appraisal> findByManagerIdWithRelationships(@Param("managerId") Long managerId);

    @Query("SELECT a FROM Appraisal a JOIN FETCH a.employee JOIN FETCH a.manager JOIN FETCH a.cycle WHERE a.cycle.id = :cycleId")
    List<Appraisal> findByCycleIdWithRelationships(@Param("cycleId") Long cycleId);
}