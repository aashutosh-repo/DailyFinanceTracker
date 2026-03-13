package com.finance.tracker.controller;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.AuthResponse;
import com.finance.tracker.dto.RegistrationRequest;
import com.finance.tracker.entity.User;
import com.finance.tracker.service.impl.AuthService;
import jakarta.servlet.http.Cookie;
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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
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
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
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

        ResponseCookie cookie = ResponseCookie.from("jwt", authResponse.getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(3600)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT_TOKEN", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // deletes cookie
//        cookie.setS/ameSite("Strict");
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }


    @GetMapping("/getUser")
    public ResponseEntity<List<User>> getUser(){
        return ResponseEntity.ok(authService.getusers());
    }
}
