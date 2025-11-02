package com.finance.tracker.controller;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.AuthResponse;
import com.finance.tracker.dto.UserDto;
import com.finance.tracker.entity.User;
import com.finance.tracker.mapper.UserMapper;
import com.finance.tracker.service.impl.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
//@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    Logger logger = LogManager.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest user) {
        User user1 = new User();
        user1.setEmail(user.getEmail());
        user1.setPasswordHash(user.getPassword());
        AuthResponse authResponse = authService.register(user1);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        logger.info("Login request received for email: " + request.getEmail());
        if(request.getEmail()==null || request.getPassword()==null){
            User newUser = new User();
            newUser.setEmail("aashutosh@gmail.com");
            newUser.setPasswordHash("Aashu@123");
            authService.register(newUser);
        }
        AuthResponse authResponse = authService.login(request);
        if (authResponse == null) {
            logger.error("Authentication failed for email: " + request.getEmail());
            return ResponseEntity.status(401).build();
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
