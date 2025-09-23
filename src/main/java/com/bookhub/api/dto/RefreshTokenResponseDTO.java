package com.bookhub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponseDTO {
    private String accessToken;    // New short-lived token (1 hour)
    private String refreshToken;   // Same refresh token (return as-is)
    private UserAccountDTO user;   // User information
}
