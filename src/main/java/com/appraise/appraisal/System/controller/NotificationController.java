package com.appraise.appraisal.System.controller;

import com.appraise.appraisal.System.config.security.AuthenticatedUser;
import com.appraise.appraisal.System.dtos.NotificationRequest;
import com.appraise.appraisal.System.dtos.NotificationResponse;
import com.appraise.appraisal.System.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * userId is no longer accepted from the caller in any form for "for me"
 * operations — every such method resolves it from the authenticated
 * user's JWT via AuthenticatedUser, the same fix already applied to
 * EmployeeController/ManagerController.
 *
 * createNotification (HR/admin manually sending a notification to
 * someone else) is the one legitimate exception — that operation is
 * inherently "send to a DIFFERENT user," so it still takes a target
 * userId, but inside the request body via NotificationRequest, not as
 * a path param.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthenticatedUser authenticatedUser;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        return new ResponseEntity<>(notificationService.sendNotification(request), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getUserNotifications(authenticatedUser.getId()));
    }

    @GetMapping("/me/unread")
    public ResponseEntity<List<NotificationResponse>> getMyUnread() {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(authenticatedUser.getId()));
    }

    @GetMapping("/me/unread/count")
    public ResponseEntity<Map<String, Long>> getMyUnreadCount() {
        long count = notificationService.getUnreadCount(authenticatedUser.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id, authenticatedUser.getId()));
    }

    @PatchMapping("/me/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead(authenticatedUser.getId());
        return ResponseEntity.noContent().build();
    }
}