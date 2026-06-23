package com.appraise.appraisal.System.mapper;

import com.appraise.appraisal.System.dtos.UserResponse;
import com.appraise.appraisal.System.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {

        if (user == null) {return null;}

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setJobTitle(user.getJobTitle());
        response.setStatus(user.getStatus());

        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        if (user.getDepartment() != null) {
            response.setDepartmentId(user.getDepartment().getId());
            response.setDepartment(user.getDepartment().getName());
        }

        if (user.getManager() != null) {
            response.setManagerId(user.getManager().getId());
            response.setManagerName(user.getManager().getName());
        }

        return response;
    }
}