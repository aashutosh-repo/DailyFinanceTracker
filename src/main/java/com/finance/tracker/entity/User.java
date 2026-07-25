package com.finance.tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.*;

/**
 * User Entity - Core user information and authentication
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_username", columnList = "username"),
    @Index(name = "idx_users_is_active", columnList = "is_active"),
    @Index(name = "idx_users_deleted_at", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true, length = 10)
    private String userId;

    @NotBlank
    @Column(nullable = false, unique = true, length = 100)
    private String username;
    
    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @NotBlank
    @Column(nullable = false, length = 255)
    private String passwordHash;
    
    @NotBlank
    @Column(nullable = false, length = 255)
    private String fullName;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 3)
    private String countryCode;
    
    @Column(length = 3)
    private String currency = "USD";
    
    @Column(columnDefinition = "TEXT")
    private String profilePicUrl;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    private java.time.LocalDate dateOfBirth;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    private java.time.LocalDateTime lastLogin;
    
    @Column(nullable = false)
    private Boolean emailVerified = false;
    
    @Column(nullable = false)
    private Boolean phoneVerified = false;
    
    @Column(nullable = false)
    private Boolean twoFactorEnabled = false;
    
    // Relationships
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Expense> expenses = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Income> income = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Budget> budgets = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Investment> investments = new HashSet<>();
    
    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
            .anyMatch(r -> r.getName().toString().equals(roleName));
    }
}
