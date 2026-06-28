package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.config.security.JwtUtil;
import com.appraise.appraisal.System.dtos.LoginRequest;
import com.appraise.appraisal.System.dtos.LoginResponse;
import com.appraise.appraisal.System.entity.User;
import com.appraise.appraisal.System.exception.InvalidCredentialsException;
import com.appraise.appraisal.System.repository.UserRepository;
import com.appraise.appraisal.System.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordMatches(request.getPassword(), user)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        LoginResponse.UserPayload payload = new LoginResponse.UserPayload(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getJobTitle()
        );

        return new LoginResponse(token, payload);
    }

    /**
     * Verifies the submitted password against whatever is stored for this
     * user, transparently supporting two formats during the migration off
     * plain-text passwords:
     *
     *   1. NEW / already-migrated users: stored value is a real BCrypt
     *      hash (always starts with $2a$, $2b$, or $2y$). Checked with
     *      passwordEncoder.matches(...) — the secure, intended path.
     *
     *   2. LEGACY users: stored value is still plain text from before
     *      this change. If the BCrypt check fails AND the stored value
     *      doesn't even look like a BCrypt hash, fall back to a direct
     *      equality check. If THAT succeeds, the password was correct —
     *      so immediately re-hash it with BCrypt and persist it, healing
     *      that row permanently. Every subsequent login for that user
     *      hits path 1 above, with no password reset and no downtime.
     *
     * Once every row in the users table has been logged into at least
     * once after this change ships, this method can be safely simplified
     * back down to just passwordEncoder.matches(...) and this whole
     * legacy branch deleted.
     */
    private boolean passwordMatches(String rawPassword, User user) {
        String stored = user.getPassword();

        if (looksLikeBcryptHash(stored)) {
            return passwordEncoder.matches(rawPassword, stored);
        }

        // Legacy plain-text row.
        boolean legacyMatch = stored.equals(rawPassword);
        if (legacyMatch) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        }
        return legacyMatch;
    }

    private boolean looksLikeBcryptHash(String value) {
        return value != null
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}