package com.appraise.appraisal.System.service;

import com.appraise.appraisal.System.dtos.LoginRequest;
import com.appraise.appraisal.System.dtos.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
