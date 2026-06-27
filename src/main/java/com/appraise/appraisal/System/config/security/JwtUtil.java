package com.appraise.appraisal.System.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates and validates JWTs, and carries the sliding-expiry logic.
 *
 * SLIDING EXPIRY DESIGN:
 * Every token is signed with a fixed lifespan (jwt.expiration — 20 min).
 * It does NOT automatically extend itself just by existing. Instead,
 * JwtAuthFilter calls generateToken(...) again on every authenticated
 * request to produce a brand-new token with a fresh 20-minute window,
 * and sends it back via the X-Refreshed-Token response header.
 *
 * So: a token is only ever "20 minutes from the last request", not
 * "20 minutes from login". If 20 minutes pass with zero requests, the
 * most recently issued token expires naturally and validateToken(...)
 * starts failing — that's what produces the logout-after-inactivity
 * behavior, with no separate "last activity" timestamp needed anywhere.
 */
@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationMillis) {
        // HMAC-SHA key built directly from the configured secret.
        // Keys.hmacShaKeyFor requires the secret be long enough for the
        // chosen algorithm; our 64-byte hex secret comfortably qualifies.
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMillis = expirationMillis;
    }

    /**
     * Creates a new signed JWT for the given user.
     * Subject = email (matches UserDetailsServiceImpl's lookup key).
     * Custom claim "role" = the user's role name, so JwtAuthFilter can
     * rebuild authorities without an extra DB hit if ever needed
     * (currently we still reload the full User via UserDetailsService
     * for correctness, but the claim is there for cheaper checks later).
     */
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /** Extracts the email (subject) from a token. Throws if invalid/expired. */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /** Extracts the role claim from a token. Throws if invalid/expired. */
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * Validates signature + expiry. Returns false for ANY problem
     * (expired, tampered, malformed) rather than throwing — callers
     * (JwtAuthFilter) just need a yes/no to decide whether to proceed
     * as authenticated or fall through as anonymous.
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * True if the token is valid but has less than half its lifespan
     * remaining. JwtAuthFilter uses this to decide whether to bother
     * issuing a refreshed token on this response — avoids reissuing
     * (and re-signing) a brand new token on literally every request
     * when the current one is still nearly fresh.
     */
    public boolean isNearingExpiry(String token) {
        Date expiration = parseClaims(token).getExpiration();
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return remaining < (expirationMillis / 2);
    }

    private Claims parseClaims(String token) {
        // parseSignedClaims throws ExpiredJwtException for expired tokens
        // and various JwtException subtypes for bad signatures/format —
        // both are left to propagate to isTokenValid's catch block above.
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}