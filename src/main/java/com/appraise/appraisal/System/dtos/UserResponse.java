package com.appraise.appraisal.System.dtos;

import com.appraise.appraisal.System.entity.enums.Roles;
import com.appraise.appraisal.System.entity.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;

    private Roles role;

    private String jobTitle;

    private UserStatus status;

    private Long departmentId;

    private String department;

    private Long managerId;
    private String managerName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}