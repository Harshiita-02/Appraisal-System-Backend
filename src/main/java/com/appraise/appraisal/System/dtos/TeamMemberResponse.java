package com.appraise.appraisal.System.dtos;

import com.appraise.appraisal.System.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {
    private Long id;
    private String name;
    private String jobTitle;
    private String department;
    private String email;
    private UserStatus status;
}