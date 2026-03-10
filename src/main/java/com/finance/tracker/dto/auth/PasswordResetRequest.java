package com.finance.tracker.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Password Reset Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PasswordResetRequest {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
}
