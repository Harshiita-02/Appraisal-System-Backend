package com.appraise.appraisal.System.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides the single PasswordEncoder bean used everywhere passwords are
 * hashed or checked: once here, injected wherever needed (AuthServiceImpl
 * to verify login, and anywhere a User is created/seeded, to hash before
 * saving).
 *
 * BCrypt is used because it's the standard for password hashing — it's
 * slow on purpose (resistant to brute-force) and automatically handles
 * salting per-password, so two users with the same password get different
 * stored hashes.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}