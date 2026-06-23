package com.appraise.appraisal.System.repository;

import com.appraise.appraisal.System.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n JOIN FETCH n.user WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdWithRelationshipsOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n JOIN FETCH n.user WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndIsReadFalseWithRelationships(@Param("userId") Long userId);

    long countByUserIdAndIsReadFalse(Long userId);
}
