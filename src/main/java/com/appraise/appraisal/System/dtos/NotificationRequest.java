package com.appraise.appraisal.System.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "Recipient User ID parameter reference is required")
    private Long userId;

    @NotBlank(message = "Notification alert title cannot be left empty")
    @Size(max = 150, message = "Notification title text must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Notification payload body message is required")
    @Size(max = 1000, message = "Notification message text body must not exceed 1000 characters")
    private String message;

    @NotBlank(message = "Functional Notification type grouping identifier is required")
    private String type; // Handled case-insensitively in the implementation layer
}