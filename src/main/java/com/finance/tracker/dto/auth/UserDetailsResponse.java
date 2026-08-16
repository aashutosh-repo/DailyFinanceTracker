package com.finance.tracker.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {
    private String userId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String profilePicture;
    private String dateOfBirth;
    private String address;
    private String city;
    private String country;
    private String countryCode;
    private String currency;
    private String createdAt;
    private String updatedAt;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean twoFactorEnabled;
}
