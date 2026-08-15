package com.finance.tracker.controller;

import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.AuthResponse;
import com.finance.tracker.service.impl.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.security.NoSuchAlgorithmException;

@RestController
public class RootAuthController {

    private final AuthService authService;

    public RootAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req, HttpServletResponse response) throws NoSuchAlgorithmException {
        AuthResponse authResponse = authService.login(req);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", authResponse.getAccessToken())
                .httpOnly(true).secure(true).path("/").sameSite("Strict").maxAge(5 * 60).build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", authResponse.getRefreshToken())
                .httpOnly(true).secure(true).path("/").sameSite("Strict").maxAge(7 * 24 * 60 * 60).build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "").httpOnly(true).secure(true).path("/").maxAge(0).build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "").httpOnly(true).secure(true).path("/").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok(java.util.Map.of("message", "Logged out"));
    }
}
