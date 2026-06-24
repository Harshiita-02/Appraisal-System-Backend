package com.appraise.appraisal.System.dtos;

import com.appraise.appraisal.System.entity.enums.AppraisalStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    // Frontend reads appraisal.cycle — expose cycleName under both names
    @JsonProperty("cycle")
    public String getCycle() {
        return cycleName;
    }

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