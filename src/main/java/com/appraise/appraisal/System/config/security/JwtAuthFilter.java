package com.appraise.appraisal.System.config.security;

import com.appraise.appraisal.System.service.implementation.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs once per incoming request, before it reaches any controller.
 *
 * RESPONSIBILITIES:
 *   1. Read the Authorization header.
 *   2. If it holds a valid "Bearer <token>", load the user and place
 *      them into SecurityContext for the rest of this request — this
 *      is what later lets controllers do
 *      ((CustomUserDetails) authentication.getPrincipal()).getUser()
 *      instead of trusting a ?employeeId query param.
 *   3. If the token is valid but more than half-expired, generate a
 *      fresh one (full new 20-minute window) and attach it to the
 *      response via X-Refreshed-Token — this is the sliding-expiry
 *      mechanism. The frontend must read this header and overwrite
 *      its stored token, or sliding expiry silently stops working
 *      and login-time expiry takes over instead.
 *   4. If there's no token, or it's invalid/expired, do nothing and
 *      let the request continue unauthenticated — SecurityConfig
 *      then decides whether the requested route requires auth, and
 *      rejects it with 401/403 if so. This filter never itself
 *      blocks a request; it only ever sets up or skips authentication.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String REFRESH_HEADER_NAME = "X-Refreshed-Token";

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HEADER_NAME);

        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            // No token presented at all — proceed unauthenticated.
            // Public endpoints (e.g. /api/auth/login) rely on this path.
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(TOKEN_PREFIX.length());

        if (!jwtUtil.isTokenValid(token)) {
            // Invalid signature OR expired — either way, treat as
            // unauthenticated rather than throwing here. SecurityConfig
            // will return 401 for any protected route reached this way.
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.extractEmail(token);

        // Only populate SecurityContext if it isn't already set for this
        // request (defensive — OncePerRequestFilter already guarantees
        // this filter itself runs once, but this guards against any
        // future filter reordering that might call doFilterInternal
        // again for the same request).
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            CustomUserDetails userDetails =
                    (CustomUserDetails) userDetailsService.loadUserByUsername(email);

            // Re-check enabled status on every request, not just at login —
            // if HR deactivates this user mid-session, their *next* request
            // (even with a still-unexpired token) gets rejected here.
            if (userDetails.isEnabled()) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Sliding expiry: only reissue when the current token has used
        // up more than half its life, to avoid re-signing a token on
        // every single request.
        if (jwtUtil.isNearingExpiry(token)) {
            String role = jwtUtil.extractRole(token);
            String refreshedToken = jwtUtil.generateToken(email, role);
            response.setHeader(REFRESH_HEADER_NAME, refreshedToken);
        }

        filterChain.doFilter(request, response);
    }
}