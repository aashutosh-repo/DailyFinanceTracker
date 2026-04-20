package com.finance.tracker.controller;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.AuthResponse;
import com.finance.tracker.dto.RegistrationRequest;
import com.finance.tracker.entity.User;
import com.finance.tracker.service.impl.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*")
//@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    Logger logger = LogManager.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
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

            // Set HttpOnly cookie for token (cannot be accessed from JavaScript)
            ResponseCookie tokenCookie = ResponseCookie.from("auth_token", authResponse.getAccessToken())
                    .httpOnly(true)  // ✅ Cannot be accessed from JavaScript
                    .secure(true)    // ✅ Only sent over HTTPS
                    .path("/")
                    .sameSite("None")
                    .maxAge(7 * 24 * 60 * 60)  // 7 days
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());

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
        // Clear auth_token cookie
        ResponseCookie tokenCookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)  // Expire immediately
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());

        // Clear user_data cookie
        ResponseCookie userCookie = ResponseCookie.from("user_data", "")
                .httpOnly(false)
                .secure(true)
                .path("/")
                .maxAge(0)  // Expire immediately
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, userCookie.toString());
        
        logger.info("User logged out successfully");
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(HttpServletRequest request) {
        try {
            // Extract token from cookies
            String token = extractTokenFromCookies(request);
            
            if (token == null) {
                logger.warn("No token found in cookies for verification");
                return ResponseEntity.status(401).body(
                    Map.of(
                        "valid", false,
                        "message", "No token found"
                    )
                );
            }
            
            // Verify token validity
            boolean isValid = authService.verifyToken(token);
            
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
            if ("auth_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        
        return null;
    }


    @GetMapping("/getUser")
    public ResponseEntity<List<User>> getUser(){
        return ResponseEntity.ok(authService.getusers());
    }
}
