package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.dtos.NotificationRequest;
import com.appraise.appraisal.System.dtos.NotificationResponse;
import com.appraise.appraisal.System.entity.Notification;
import com.appraise.appraisal.System.entity.User;
import com.appraise.appraisal.System.entity.enums.NotificationType;
import com.appraise.appraisal.System.exception.BadRequestException;
import com.appraise.appraisal.System.exception.ResourceNotFoundException;
import com.appraise.appraisal.System.mapper.NotificationMapper;
import com.appraise.appraisal.System.repository.NotificationRepository;
import com.appraise.appraisal.System.repository.UserRepository;
import com.appraise.appraisal.System.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NotificationResponse sendNotification(NotificationRequest request) {
        if (request == null) {
            throw new BadRequestException("Notification request payload cannot be null");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Target recipient user not found with ID: " + request.getUserId()));

        Notification notification = NotificationMapper.toEntity(request, user);
        Notification saved = notificationRepository.save(notification);
        return NotificationMapper.toResponse(saved);
    }

    /**
     * Internal helper — creates and persists a notification without going through the DTO layer.
     * Called by other services (AppraisalService, ReviewService, GoalService) to auto-notify users.
     */
    @Override
    @Transactional
    public void createInternalNotification(User user, String title, String message, NotificationType type) {
        if (user == null) return;

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationResponse> getUserNotifications(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return notificationRepository.findByUserIdWithRelationshipsOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return notificationRepository.findByUserIdAndIsReadFalseWithRelationships(userId)
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        notification.setIsRead(true);
        Notification updated = notificationRepository.save(notification);
        return NotificationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseWithRelationships(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    public long getUnreadCount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
}
