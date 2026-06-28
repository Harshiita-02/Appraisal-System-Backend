package com.appraise.appraisal.System.config;

import com.appraise.appraisal.System.config.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central security policy: which routes are public, which need a role,
 * and where JwtAuthFilter sits in the request pipeline.
 *
 * ROLE MODEL (from Roles enum): HR, MANAGER, EMPLOYEE.
 *   - /api/auth/**     -> public (login has to be reachable before
 *                         anyone has a token)
 *   - GET /api/hr/cycles -> any authenticated user. Cycles are shared
 *                         reference data (managers building reports,
 *                         employees filtering appraisals, HR creating
 *                         them) — not HR-exclusive despite living under
 *                         /api/hr. Declared before the blanket HR rule
 *                         below since Spring Security matches top-down.
 *   - /api/hr/**        -> HR only (everything else under this prefix)
 *   - /api/manager/**  -> MANAGER only (covers both "managing my team"
 *                         endpoints like /team, /goals, AND "acting as
 *                         an employee" endpoints like /my-appraisals,
 *                         /my-goals — all live under this one prefix
 *                         in ManagerController, all require a MANAGER)
 *   - /api/employee/** -> EMPLOYEE only. Confirmed intentional: manager
 *                         and employee dashboards/UIs are entirely
 *                         separate in the frontend (different layouts,
 *                         different functionality, no shared components
 *                         calling across the boundary), so locking
 *                         managers out of /api/employee/** entirely is
 *                         both correct and the more secure (least-
 *                         privilege) choice.
 *
 * CSRF is disabled because this is a stateless REST API authenticated
 * by JWT in the Authorization header, not by session cookies — CSRF
 * protection exists specifically to defend cookie-based session auth,
 * so it doesn't apply here and would only get in the way of non-browser
 * clients (Postman, mobile apps, etc.).
 *
 * Session policy is STATELESS — Spring Security must never create or
 * rely on an HttpSession; every request re-authenticates itself purely
 * from its JWT, via JwtAuthFilter.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // Cycles are read-only reference data needed by every
                        // role (managers building reports, employees filtering
                        // appraisals by cycle, HR creating them) — so this one
                        // /hr/** sub-path is opened to any authenticated user
                        // BEFORE the blanket HR-only rule below. Order matters:
                        // Spring Security checks rules top-to-bottom and stops
                        // at the first match, so this must come first.
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/hr/cycles").authenticated()
                        .requestMatchers("/api/hr/**").hasRole("HR")
                        .requestMatchers("/api/manager/**").hasRole("MANAGER")
                        .requestMatchers("/api/employee/**").hasRole("EMPLOYEE")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}