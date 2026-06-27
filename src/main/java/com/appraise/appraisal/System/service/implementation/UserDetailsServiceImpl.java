package com.appraise.appraisal.System.service.implementation;

import com.appraise.appraisal.System.config.security.CustomUserDetails;
import com.appraise.appraisal.System.entity.User;
import com.appraise.appraisal.System.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Tells Spring Security how to load a user, given just an email address.
 *
 * Spring Security calls loadUserByUsername(...) in two situations:
 *   1. During login — AuthServiceImpl will call this (indirectly, via
 *      AuthenticationManager) to fetch the user record before checking
 *      the password.
 *   2. During every authenticated request — JwtAuthFilter calls this
 *      after extracting the email from a valid JWT, to reload the full
 *      User from the DB and put it into SecurityContext for that request.
 *
 * "username" in the method name is just Spring Security's generic term —
 * we're feeding it an email, since that's our actual unique login field.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        return new CustomUserDetails(user);
    }
}