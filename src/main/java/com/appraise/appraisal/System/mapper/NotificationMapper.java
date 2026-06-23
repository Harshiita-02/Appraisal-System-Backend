package com.appraise.appraisal.System.mapper;

import com.appraise.appraisal.System.dtos.NotificationRequest;
import com.appraise.appraisal.System.dtos.NotificationResponse;
import com.appraise.appraisal.System.entity.Notification;
import com.appraise.appraisal.System.entity.User;
import com.appraise.appraisal.System.entity.enums.NotificationType;
import com.appraise.appraisal.System.exception.BadRequestException;

public class NotificationMapper {

    public static Notification toEntity(NotificationRequest request, User user) {
        if (request == null) return null;

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(request.getTitle().trim());
        notification.setMessage(request.getMessage().trim());

        if (request.getType() == null || request.getType().trim().isBlank()) {
            throw new BadRequestException("Notification classification type cannot be null or empty");
        }

        try {
            notification.setType(NotificationType.valueOf(request.getType().trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid standard notification category type value provided: '" + request.getType() + "'");
        }

        return notification;
    }

    public static NotificationResponse toResponse(Notification notification) {
        if (notification == null) return null;

        return new NotificationResponse(
                notification.getId(),
                notification.getUser() != null ? notification.getUser().getId() : null,
                notification.getTitle(),
                notification.getMessage(),
                notification.getType() != null ? notification.getType().name() : null,
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}