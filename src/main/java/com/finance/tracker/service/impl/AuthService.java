package com.finance.tracker.service.impl;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.AuthResponse;
import com.finance.tracker.dto.UserDto;
import com.finance.tracker.entity.User;
import com.finance.tracker.mapper.UserMapper;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthResponse login(AuthRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtService.generateToken(user.getEmail());
        UserDto userDto = UserMapper.toDto(user);
        return new AuthResponse(token,userDto);
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
