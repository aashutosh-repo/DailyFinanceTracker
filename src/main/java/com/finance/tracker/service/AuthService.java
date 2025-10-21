package com.finance.tracker.service;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.AuthResponse;
import com.finance.tracker.entity.User;
import com.finance.tracker.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
        return new AuthResponse(token);
    }

    @PostConstruct
    public void createDemoUser() {
        if (userRepository.findByEmail("aashtosh@gmail.com").isEmpty()) {
            User u = User.builder()
                    .email("user@example.com")
                    .passwordHash(encoder.encode("Aashu@123"))
                    .fullName("Demo User")
                    .build();
            userRepository.save(u);
        }
    }
}
