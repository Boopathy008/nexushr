package com.nexushr.dto.response.auth;

import com.nexushr.domain.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String username;
    private String email;
    private Role role;
    private UUID userId;
}
