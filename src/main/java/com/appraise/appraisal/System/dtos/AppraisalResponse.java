package com.appraise.appraisal.System.dtos;

import com.appraise.appraisal.System.entity.enums.AppraisalStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppraisalResponse {

    private Long id;

    private Long employeeId;
    private String employeeName;

    private Long managerId;
    private String managerName;

    private Long departmentId;
    private String department;

    private Long cycleId;
    private String cycleName;

    private Double selfRating;
    private Double managerRating;

    private String whatWentWell;
    private String whatToImprove;
    private String keyAchievements;

    private String managerComments;

    private AppraisalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}