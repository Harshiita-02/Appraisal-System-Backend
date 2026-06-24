package com.appraise.appraisal.System.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserPayload user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPayload {
        private Long id;
        private String name;
        private String email;
        private String role;
        private String jobTitle;
    }
}