package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.UserRequest;
import com.appraise.appraisal.System.dtos.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest request);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);

    UserResponse updateUserStatus(Long id, String status);
}