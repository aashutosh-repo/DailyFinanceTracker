package com.finance.tracker.service.impl;

import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.AuthResponse;
import com.finance.tracker.dto.UserDto;
import com.finance.tracker.entity.User;
import com.finance.tracker.mapper.UserMapper;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.entity.RefreshToken;
import com.finance.tracker.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenStore accessTokenStore;

    public AuthResponse register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Username already exists");
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        User savedUser = userRepository.save(user);
        UserDto dto = UserMapper.toDto(savedUser);

        String token = jwtService.generateToken(savedUser.getEmail());

        return new AuthResponse(token, dto);
    }

    public AuthResponse login(AuthRequest req) throws NoSuchAlgorithmException {
        Optional<User> user = userRepository.findByEmail(req.getEmail());
        if(user.isEmpty()) {
            user = userRepository.findByUsername(req.getEmail());
        }
        if(user.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.get().getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        UserDto userDto = UserMapper.toDto(user.get());

        // Create short-lived opaque access token (5 minutes)
        String accessToken = accessTokenStore.createToken(user.get().getEmail(), 60 * 5);

        // Create refresh token stored in DB (7 days)
        String refreshTokenValue = randomUUID().toString() + "-" + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(java.security.SecureRandom.getInstanceStrong().generateSeed(16));
        java.time.LocalDateTime expiresAt = java.time.LocalDateTime.now().plusDays(7);
        // compute SHA-256 hash for storage
        String hash;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(refreshTokenValue.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            hash = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash refresh token", e);
        }
        RefreshToken rt = RefreshToken.builder()
                .user(user.get())
                .token(refreshTokenValue)
                .tokenHash(hash)
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(rt);

        AuthResponse resp = new AuthResponse(accessToken, userDto);
        resp.setRefreshToken(refreshTokenValue);
        return resp;
    }

    public Authentication getAuthentication(String token) {
        // Extract username from token
        String username = jwtService.extractUsername(token);

        // Load user details
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // Create Authentication object
        return new UsernamePasswordAuthenticationToken(
                user, null
        );
    }

    public List<User> getusers() {
        return userRepository.findAll();
    }

    /**
     * Verify if a JWT token is valid
     */
    public boolean verifyToken(String token) {
        try {
            String email = jwtService.extractUsername(token);
            return email != null && !email.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public UserDto authenticate(String token) {
        String userName= jwtService.extractUsername(token);
        User user = userRepository.findByEmail(userName).orElseThrow(()-> new RuntimeException("User Not Found"));
        return UserMapper.toDto(user);
    }

//    @PostConstruct
//    public void createDemoUser() {
//        if (userRepository.findByEmail("aashu@gmail.com").isEmpty()) {
//            User u = User.builder()
//                    .email("ashu@gmail.com")
//                    .passwordHash(encoder.encode("ashu@123"))
//                    .fullName("Demo User")
//                    .build();
//            userRepository.save(u);
//        }
//    }
}
