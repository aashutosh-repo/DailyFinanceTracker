package com.finance.tracker.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registration Request DTO
 * Contains all required and optional fields for user registration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain uppercase letter, lowercase letter, number, and special character")
    private String password;
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid (optional)")
    private String phone;
    
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be 2-letter ISO code (optional)")
    private String countryCode;
    
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3-letter ISO code (optional)")
    private String currency;
}
