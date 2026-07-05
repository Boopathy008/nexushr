package com.nexushr.service;

import com.nexushr.domain.entity.User;
import com.nexushr.domain.enums.Role;
import com.nexushr.dto.request.auth.LoginRequest;
import com.nexushr.dto.request.auth.RefreshTokenRequest;
import com.nexushr.dto.request.auth.RegisterRequest;
import com.nexushr.dto.response.auth.AuthResponse;
import com.nexushr.exception.BusinessRuleException;
import com.nexushr.exception.DuplicateResourceException;
import com.nexushr.exception.ResourceNotFoundException;
import com.nexushr.repository.UserRepository;
import com.nexushr.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.EMPLOYEE)
                .active(true)
                .build();

        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());
        user.setRefreshToken(passwordEncoder.encode(refreshToken));
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));

        userRepository.save(user);

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        return AuthResponse.builder()
                .accessToken(tokenProvider.generateAccessToken(auth))
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .userId(user.getId())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(), request.getPassword()));

        User user = userRepository
                .findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new BusinessRuleException("Account is deactivated. Contact administrator.");
        }

        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());
        user.setRefreshToken(passwordEncoder.encode(refreshToken));
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(tokenProvider.generateAccessToken(auth))
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .userId(user.getId())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new BusinessRuleException("Invalid or expired refresh token");
        }
        String username = tokenProvider.getUsernameFromToken(request.getRefreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Refresh token has expired. Please login again.");
        }

        String newAccessToken = tokenProvider.generateAccessTokenFromUsername(username);
        String newRefreshToken = tokenProvider.generateRefreshToken(username);

        user.setRefreshToken(passwordEncoder.encode(newRefreshToken));
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .userId(user.getId())
                .build();
    }

    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);
        });
    }
}
