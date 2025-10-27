package com.finance.tracker.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Data
public class AuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserDto user;

    public AuthResponse(String token,UserDto user) {
        this.accessToken = token;
        tokenType = "Bearer";
        this.user = Objects.requireNonNullElseGet(user, () -> new UserDto("1", "aashutosh@gmail.com", "Aashutosh"));
    }
}
