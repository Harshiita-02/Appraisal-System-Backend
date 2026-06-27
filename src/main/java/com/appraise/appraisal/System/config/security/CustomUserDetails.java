package com.appraise.appraisal.System.config.security;

import com.appraise.appraisal.System.entity.User;
import com.appraise.appraisal.System.entity.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapts our own User entity to Spring Security's UserDetails interface.
 * Spring Security has no idea what a "User" entity is — everywhere it
 * needs to check who's logged in, what their roles are, or whether their
 * account is usable, it talks to this wrapper instead.
 *
 * One real User field maps to one Spring Security concept:
 *   - email          -> getUsername() (the unique login identifier)
 *   - password       -> getPassword() (the BCrypt hash, never raw text)
 *   - role           -> getAuthorities() (prefixed with "ROLE_" per
 *                        Spring Security convention, so hasRole("HR")
 *                        in SecurityConfig matches a User with role HR)
 *   - status         -> isEnabled() (an INACTIVE user can't log in,
 *                        even with a correct password)
 */
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /** Exposes the wrapped entity so controllers/services can pull the
     *  real id, department, manager, etc. straight off SecurityContext
     *  without a second DB lookup. */
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }
}