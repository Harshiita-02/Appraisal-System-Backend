package com.appraise.appraisal.System.controller;

import com.appraise.appraisal.System.dtos.LoginRequest;
import com.appraise.appraisal.System.dtos.LoginResponse;
import com.appraise.appraisal.System.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}