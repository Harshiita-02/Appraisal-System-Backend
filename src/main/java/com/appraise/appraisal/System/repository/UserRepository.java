package com.appraise.appraisal.System.repository;

import com.appraise.appraisal.System.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department LEFT JOIN FETCH u.manager")
    List<User> findAllWithRelationships();

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department LEFT JOIN FETCH u.manager WHERE u.id = :id")
    Optional<User> findByIdWithRelationships(@Param("id") Long id);

    boolean existsByManagerId(Long managerId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department LEFT JOIN FETCH u.manager WHERE u.manager.id = :managerId")
    List<User> findByManagerIdWithRelationships(@Param("managerId") Long managerId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.department LEFT JOIN FETCH u.manager WHERE u.department.id = :departmentId")
    List<User> findByDepartmentIdWithRelationships(@Param("departmentId") Long departmentId);

    List<User> findByRole(com.appraise.appraisal.System.entity.enums.Roles role);
}