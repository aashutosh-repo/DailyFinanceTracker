package com.finance.tracker.controller;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.AuthResponse;
import com.finance.tracker.dto.RegistrationRequest;
import com.finance.tracker.dto.auth.PasswordChangeRequest;
import com.finance.tracker.dto.auth.PasswordChangeResponse;
import com.finance.tracker.dto.auth.UserDetailsResponse;
import com.finance.tracker.dto.auth.UserProfileUpdateRequest;
import com.finance.tracker.entity.User;
import com.finance.tracker.mapper.UserMapper;
import com.finance.tracker.service.impl.AccessTokenStore;
import com.finance.tracker.service.impl.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*")
//@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AccessTokenStore accessTokenStore;
    private final com.finance.tracker.repository.RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    Logger logger = LogManager.getLogger(AuthController.class);

    public AuthController(AuthService authService, AccessTokenStore accessTokenStore,
                         com.finance.tracker.repository.RefreshTokenRepository refreshTokenRepository,
                         PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.accessTokenStore = accessTokenStore;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegistrationRequest registrationRequest) {
        logger.info("Registration request received for email: {}", registrationRequest.getEmail());

        User user = User.builder()
                .username(registrationRequest.getUsername())
                .email(registrationRequest.getEmail())
                .passwordHash(registrationRequest.getPassword())
                .fullName(registrationRequest.getFullName())
                .phone(registrationRequest.getPhone())
                .countryCode(registrationRequest.getCountryCode())
                .currency(registrationRequest.getCurrency() != null ? registrationRequest.getCurrency() : "USD")
                .isActive(true)
                .emailVerified(false)
                .phoneVerified(false)
                .twoFactorEnabled(false)
                .build();

        AuthResponse authResponse = authService.register(user);
        logger.info("User registered successfully: {}", registrationRequest.getEmail());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, HttpServletResponse response) {
        logger.info("Login request received for email: {}", request.getEmail());

        // Validate input
        if (request.getEmail() == null || request.getEmail().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            logger.error("Invalid login credentials - email or password is empty");
            return ResponseEntity.status(401).body(
                    AuthResponse.builder()
                            .success(false)
                            .message("Email and password are required")
                            .build()
            );
        }

        try {
            AuthResponse authResponse = authService.login(request);
            if (authResponse == null) {
                logger.error("Authentication failed for email: {}", request.getEmail());
                return ResponseEntity.status(401).body(
                        AuthResponse.builder()
                                .success(false)
                                .message("Invalid credentials")
                                .build()
                );
            }

            // Set short-lived access token cookie
            ResponseCookie accessCookie = ResponseCookie.from("access_token", authResponse.getAccessToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(5 * 60) // 5 minutes
                    .build();

            // Set refresh token cookie (HttpOnly, longer lived)
            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", authResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            logger.info("User logged in successfully: {}", request.getEmail());
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            logger.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(
                    AuthResponse.builder()
                            .success(false)
                            .message(e.getMessage() != null ? e.getMessage() : "Invalid credentials")
                            .build()
            );
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return ResponseEntity.status(500).body(
                    AuthResponse.builder()
                            .success(false)
                            .message("An unexpected error occurred")
                            .build()
            );
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear access_token cookie
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        // Clear refresh_token cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // Clearing cookies is primary guarantee; token revocation is handled server-side via refresh token rotation.

        logger.info("User logged out successfully");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(HttpServletRequest request) {
        try {
            // Try Authorization header first (Bearer token), then cookies
            String token = null;
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }

            if (token == null) {
                token = extractTokenFromCookies(request);
            }

            if (token == null) {
                logger.warn("No token found in Authorization header or cookies for verification");
                return ResponseEntity.status(401).body(Map.of("valid", false, "message", "No token found"));
            }

            // Verify token validity using AccessTokenStore (opaque tokens created at login)
            boolean isValid = accessTokenStore.validate(token);

            if (isValid) {
                logger.debug("Token verification successful");
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "message", "Token is valid"
                ));
            } else {
                logger.warn("Token verification failed - invalid token");
                return ResponseEntity.status(401).body(Map.of(
                        "valid", false,
                        "message", "Token is invalid"
                ));
            }
        } catch (Exception e) {
            logger.error("Error verifying token", e);
            return ResponseEntity.status(401).body(Map.of(
                    "valid", false,
                    "message", "Token verification failed: " + e.getMessage()
            ));
        }
    }

    /**
     * Extract JWT token from cookies
     */
    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<UserDetailsResponse> getUser(@PathVariable String userId) {
        log.info("get User details: {} ", userId);
        User u = authService.getUserDetailsById(userId);
        UserDetailsResponse userDetails = UserMapper.toUserDetails(u);
        return ResponseEntity.ok(userDetails);
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<UserDetailsResponse> updateUser(@PathVariable String userId,
                                                        @RequestBody UserProfileUpdateRequest request) {
        log.info("Update user profile for userId: {}", userId);
        User updatedUser = authService.updateUserProfile(userId, request);
        return ResponseEntity.ok(UserMapper.toUserDetails(updatedUser));
    }

    @PostMapping("/user/{userId}/change-password")
    public ResponseEntity<PasswordChangeResponse> changePassword(@PathVariable String userId,
                                                                @RequestBody PasswordChangeRequest request) {
        log.info("Change password request for userId: {}", userId);

        if (request == null || request.getOldPassword() == null || request.getNewPassword() == null || request.getConfirmPassword() == null) {
            return ResponseEntity.badRequest().body(new PasswordChangeResponse(false, "Password fields are required"));
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(new PasswordChangeResponse(false, "New password and confirm password do not match"));
        }

        User user = authService.getUserDetailsById(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body(new PasswordChangeResponse(false, "Current password is incorrect"));
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        authService.saveUser(user);

        return ResponseEntity.ok(new PasswordChangeResponse(true, "Password changed successfully"));
    }

    @PostMapping("/user/{userId}/profile-picture")
    public ResponseEntity<UserDetailsResponse> uploadProfilePicture(@PathVariable String userId,
                                                                   @RequestParam("file") MultipartFile file) {
        log.info("Upload profile picture for userId: {}", userId);
        System.out.println("File size: " + file.getSize());
        System.out.println("File name: " + file.getOriginalFilename());

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String profilePicUrl = "data:" + file.getContentType() + ";base64," + java.util.Base64.getEncoder().encodeToString(file.getBytes());
            User updatedUser = authService.updateProfilePicture(userId, profilePicUrl);
            return ResponseEntity.ok(UserMapper.toUserDetails(updatedUser));
        } catch (IOException e) {
            log.error("Error uploading profile picture for userId: {}", userId, e);
            return ResponseEntity.status(500).build();
        }
    }
}
