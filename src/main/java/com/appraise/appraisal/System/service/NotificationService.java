package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.NotificationRequest;
import com.appraise.appraisal.System.dtos.NotificationResponse;
import com.appraise.appraisal.System.entity.User;
import com.appraise.appraisal.System.entity.enums.NotificationType;

import java.util.List;

public interface NotificationService {

    NotificationResponse sendNotification(NotificationRequest request);

    // Internal method — called by other services to auto-generate notifications
    void createInternalNotification(User user, String title, String message, NotificationType type);

    List<NotificationResponse> getUserNotifications(Long userId);

    List<NotificationResponse> getUnreadNotifications(Long userId);

    NotificationResponse markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    long getUnreadCount(Long userId);
}
