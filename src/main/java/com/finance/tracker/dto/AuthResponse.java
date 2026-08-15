package com.finance.tracker.dto;
import lombok.*;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserDto user;
    private boolean success;
    private String message;

    public AuthResponse(String token, UserDto user) {
        this.accessToken = token;
        this.tokenType = "Bearer";
        this.user = Objects.requireNonNullElseGet(user, () -> new UserDto("1", "aashutosh@gmail.com", "Aashutosh"));
        this.success = true;
    }
}
