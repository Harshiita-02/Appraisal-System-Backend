package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.dtos.LoginRequest;
import com.appraise.appraisal.System.dtos.LoginResponse;
import com.appraise.appraisal.System.entity.User;
import com.appraise.appraisal.System.repository.UserRepository;
import com.appraise.appraisal.System.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // No JWT for now — sending a plain token string.
        // Replace with real JWT generation when you add Spring Security.
        String token = "token-for-user-" + user.getId();

        LoginResponse.UserPayload payload = new LoginResponse.UserPayload(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getJobTitle()   // make sure your User entity has getJobTitle()
        );

        return new LoginResponse(token, payload);
    }
}