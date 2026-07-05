package com.nexushr.security;

import com.nexushr.domain.entity.User;
import com.nexushr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by username OR email.
     *
     * Spring Security calls this method:
     *   1. During login — AuthenticationManager passes the usernameOrEmail value
     *   2. On every JWT-authenticated request — JwtAuthenticationFilter
     *      calls this after extracting the username claim from the token.
     *
     * Role is prefixed with "ROLE_" as required by Spring Security's
     * hasRole() / hasAnyRole() matchers.
     *
     * @param usernameOrEmail the username or email from the JWT subject claim
     * @throws UsernameNotFoundException if no matching active user is found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {

        User user = userRepository
                .findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", usernameOrEmail);
                    return new UsernameNotFoundException(
                            "User not found with username or email: " + usernameOrEmail);
                });

        if (!user.isActive()) {
            log.warn("Disabled account login attempt: {}", usernameOrEmail);
            throw new UsernameNotFoundException(
                    "Account is deactivated: " + usernameOrEmail);
        }

        // Spring Security requires the "ROLE_" prefix for hasRole() to work
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.isActive())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}
