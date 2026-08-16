package com.finance.tracker.mapper;

import com.finance.tracker.dto.TransactionDto;
import com.finance.tracker.dto.UserDto;
import com.finance.tracker.dto.auth.UserDetailsResponse;
import com.finance.tracker.entity.Transaction;
import com.finance.tracker.entity.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getFullName());
        return dto;
    }

    public static UserDetailsResponse toUserDetails(User user) {
        if (user == null) {
            return null;
        }

        String fullName = user.getFullName() != null ? user.getFullName().trim() : "";
        String firstName = "";
        String lastName = "";

        if (!fullName.isEmpty()) {
            String[] nameParts = fullName.split("\\s+", 2);
            firstName = nameParts[0];
            if (nameParts.length > 1) {
                lastName = nameParts[1];
            }
        }

        UserDetailsResponse response = new UserDetailsResponse();
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFirstName(firstName);
        response.setLastName(lastName);
        response.setFullName(fullName);
        response.setPhoneNumber(user.getPhone());
        response.setProfilePicture(user.getProfilePicUrl());
        response.setDateOfBirth(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
        response.setAddress(null);
        response.setCity(null);
        response.setCountry(user.getCountryCode());
        response.setCountryCode(user.getCountryCode());
        response.setCurrency(user.getCurrency());
        response.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        response.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
        response.setEmailVerified(Boolean.TRUE.equals(user.getEmailVerified()));
        response.setPhoneVerified(Boolean.TRUE.equals(user.getPhoneVerified()));
        response.setTwoFactorEnabled(Boolean.TRUE.equals(user.getTwoFactorEnabled()));
        return response;
    }

    public static User toUser(UserDto user) {
        if (user == null) {
            return null;
        }
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setFullName(user.getName());return newUser;
    }
}
