package com.finance.tracker.service.impl;
import com.finance.tracker.dto.AuthRequest;
import com.finance.tracker.dto.AuthResponse;
import com.finance.tracker.dto.UserDto;
import com.finance.tracker.entity.ExpenseCategory;
import com.finance.tracker.entity.User;
import com.finance.tracker.mapper.UserMapper;
import com.finance.tracker.repository.ExpenseCategoryRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthResponse register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Username already exists");
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        User savedUser = userRepository.save(user);
        
        // Create default categories for the new user
        createDefaultCategories(savedUser);
        
        UserDto dto = UserMapper.toDto(savedUser);

        String token = jwtService.generateToken(savedUser.getEmail());

        return new AuthResponse(token, dto);
    }
    
    private void createDefaultCategories(User user) {
        String[][] defaultCategories = {
            {"EDUCATION", "#FF6B6B"},
            {"INVESTMENT", "#4ECDC4"},
            {"UTILITY", "#FFE66D"},
            {"SHOPPING", "#95E1D3"},
            {"GROCERY", "#C0C0FF"},
            {"TRAVELLING", "#FF9E9E"},
            {"ADVENTURE", "#7FDBCA"},
            {"OTHER", "#FF8B9E"}
        };
        
        for (String[] category : defaultCategories) {
            ExpenseCategory expenseCategory = ExpenseCategory.builder()
                    .user(user)
                    .name(category[0])
                    .colorCode(category[1])
                    .isDefault(true)
                    .build();
            expenseCategoryRepository.save(expenseCategory);
        }
    }

    public AuthResponse login(AuthRequest req) {
        Optional<User> user = userRepository.findByEmail(req.getEmail());
        if(user.isEmpty()) {
            user = userRepository.findByUsername(req.getEmail());
        }
        if(user.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }
        if (!encoder.matches(req.getPassword(), user.get().getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtService.generateToken(user.get().getEmail());
        UserDto userDto = UserMapper.toDto(user.get());
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
