package com.appraise.appraisal.System.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // NEW: without this, the browser receives the
                // X-Refreshed-Token header on each response but blocks
                // JavaScript from reading it (default browser CORS
                // behavior hides custom response headers unless
                // explicitly exposed). JwtAuthFilter sends a renewed
                // token via this header as part of sliding expiry — if
                // it's not exposed here, the frontend can never see the
                // refreshed token, and users get logged out after the
                // token's original 20-minute window regardless of how
                // active they were.
                .exposedHeaders("X-Refreshed-Token")
                .allowCredentials(false);
    }
}