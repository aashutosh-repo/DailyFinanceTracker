# Testing Guide - Daily Personal Finance Tracker

## Testing Strategy

### Test Pyramid

```
          /\
         /E2E\           End-to-End Tests (5-10%)
        /------\
       / Integration\  Integration Tests (20-30%)
      /-----------\
     /   Unit      \  Unit Tests (60-70%)
    /_______________\
```

---

## Unit Testing (JUnit 5 + Mockito)

### Service Layer Unit Tests

```java
package com.personalfinance.service;

import com.finance.tracker.entity.Expense;
import com.finance.tracker.entity.ExpenseCategory;
import com.finance.tracker.entity.User;
import com.finance.tracker.dto.expense.*;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.ExpenseCategoryRepository;
import com.finance.tracker.repository.ExpenseRepository;
import com.finance.tracker.service.ExpenseService;
import com.personalfinance.mapper.ExpenseMapper;
import com.personalfinance.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Expense Service Tests")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseCategoryRepository categoryRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser;
    private ExpenseCategory testCategory;
    private Expense testExpense;
    private ExpenseRequest expenseRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        testCategory = ExpenseCategory.builder()
                .id(1L)
                .user(testUser)
                .name("Food & Dining")
                .colorCode("#FF6B6B")
                .build();

        testExpense = Expense.builder()
                .id(1L)
                .user(testUser)
                .category(testCategory)
                .description("Grocery shopping")
                .amount(BigDecimal.valueOf(45.99))
                .currency("USD")
                .expenseDate(LocalDate.now())
                .build();

        expenseRequest = ExpenseRequest.builder()
                .description("Grocery shopping")
                .amount(BigDecimal.valueOf(45.99))
                .categoryId(1L)
                .expenseDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Should create expense successfully")
    void testCreateExpense_Success() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(java.util.Optional.of(testCategory));
        when(expenseRepository.save(any(Expense.class))).thenReturn(testExpense);
        when(expenseMapper.toResponse(any(Expense.class)))
                .thenReturn(ExpenseResponse.builder()
                        .id(1L)
                        .description("Grocery shopping")
                        .amount(BigDecimal.valueOf(45.99))
                        .build());

        // Act
        ExpenseResponse response = expenseService.createExpense(1L, expenseRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Grocery shopping", response.getDescription());
        assertEquals(BigDecimal.valueOf(45.99), response.getAmount());

        // Verify interactions
        verify(categoryRepository).findById(1L);
        verify(expenseRepository).save(any(Expense.class));
        verify(cacheService).invalidateUserExpenses(1L);
    }

    @Test
    @DisplayName("Should throw exception for zero amount")
    void testCreateExpense_ZeroAmount() {
        // Arrange
        ExpenseRequest invalidRequest = ExpenseRequest.builder()
                .description("Test")
                .amount(BigDecimal.ZERO)
                .categoryId(1L)
                .expenseDate(LocalDate.now())
                .build();

        // Act & Assert
        assertThrows(ValidationException.class, () ->
                expenseService.createExpense(1L, invalidRequest)
        );

        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception for non-existent category")
    void testCreateExpense_CategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                expenseService.createExpense(1L, expenseRequest)
        );
    }

    @Test
    @DisplayName("Should get total expenses for date range")
    void testGetTotalExpenses() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 31);
        BigDecimal expectedTotal = BigDecimal.valueOf(2500.50);

        when(expenseRepository.sumExpensesByUserAndDateRange(1L, startDate, endDate))
                .thenReturn(expectedTotal);

        // Act
        BigDecimal result = expenseService.getTotalExpenses(1L, startDate, endDate);

        // Assert
        assertEquals(expectedTotal, result);
        verify(expenseRepository).sumExpensesByUserAndDateRange(1L, startDate, endDate);
    }

    @Test
    @DisplayName("Should get expenses by category")
    void testGetExpensesByCategory() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 31);

        when(expenseRepository.findByUserAndDateRange(1L, startDate, endDate))
                .thenReturn(java.util.List.of(testExpense));
        when(expenseRepository.sumExpensesByUserAndDateRange(1L, startDate, endDate))
                .thenReturn(BigDecimal.valueOf(45.99));

        // Act
        var result = expenseService.getExpensesByCategory(1L, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("Food & Dining", result.get(0).getCategoryName());
    }

    @Test
    @DisplayName("Should update expense successfully")
    void testUpdateExpense_Success() {
        // Arrange
        ExpenseRequest updateRequest = ExpenseRequest.builder()
                .description("Updated description")
                .amount(BigDecimal.valueOf(50.00))
                .categoryId(1L)
                .expenseDate(LocalDate.now())
                .build();

        Expense updatedExpense = testExpense;
        updatedExpense.setDescription("Updated description");
        updatedExpense.setAmount(BigDecimal.valueOf(50.00));

        when(expenseRepository.findById(1L)).thenReturn(java.util.Optional.of(testExpense));
        when(categoryRepository.findById(1L)).thenReturn(java.util.Optional.of(testCategory));
        when(expenseRepository.save(any(Expense.class))).thenReturn(updatedExpense);
        when(expenseMapper.toResponse(any(Expense.class)))
                .thenReturn(ExpenseResponse.builder()
                        .id(1L)
                        .description("Updated description")
                        .amount(BigDecimal.valueOf(50.00))
                        .build());

        // Act
        ExpenseResponse response = expenseService.updateExpense(1L, 1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Updated description", response.getDescription());
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    @DisplayName("Should delete expense successfully")
    void testDeleteExpense_Success() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(java.util.Optional.of(testExpense));

        // Act
        expenseService.deleteExpense(1L, 1L, "testuser");

        // Assert
        verify(expenseRepository).save(any(Expense.class));
        verify(cacheService).invalidateUserExpenses(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting other user's expense")
    void testDeleteExpense_Forbidden() {
        // Arrange
        User otherUser = User.builder().id(2L).build();
        testExpense.setUser(otherUser);

        when(expenseRepository.findById(1L)).thenReturn(java.util.Optional.of(testExpense));

        // Act & Assert
        assertThrows(ForbiddenException.class, () ->
                expenseService.deleteExpense(1L, 1L, "testuser")
        );
    }
}
```

---

## Integration Testing

```java
package com.personalfinance.integration;

import com.finance.tracker.entity.Expense;
import com.finance.tracker.entity.ExpenseCategory;
import com.finance.tracker.entity.User;
import com.finance.tracker.repository.ExpenseCategoryRepository;
import com.finance.tracker.repository.ExpenseRepository;
import com.finance.tracker.repository.UserRepository;
import com.personalfinance.DailyFinanceTrackerApplication;
import com.finance.tracker.dto.expense.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DailyFinanceTrackerApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
@DisplayName("Expense API Integration Tests")
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseCategoryRepository categoryRepository;

    private User testUser;
    private ExpenseCategory testCategory;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashed_password")
                .fullName("Test User")
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);

        // Create test category
        testCategory = ExpenseCategory.builder()
                .user(testUser)
                .name("Food & Dining")
                .colorCode("#FF6B6B")
                .build();
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should create expense via API")
    void testCreateExpense_API() throws Exception {
        // Arrange
        ExpenseRequest request = ExpenseRequest.builder()
                .description("Grocery shopping")
                .amount(BigDecimal.valueOf(45.99))
                .categoryId(testCategory.getId())
                .expenseDate(LocalDate.now())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.description").value("Grocery shopping"))
                .andExpect(jsonPath("$.data.amount").value(45.99));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get expenses with pagination")
    void testGetExpenses_Paginated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/expenses")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.pageNumber").value(0))
                .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should get expenses with date filter")
    void testGetExpenses_DateFilter() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/expenses")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should update expense")
    void testUpdateExpense() throws Exception {
        // Create test expense first
        Expense expense = Expense.builder()
                .user(testUser)
                .category(testCategory)
                .description("Original description")
                .amount(BigDecimal.valueOf(45.99))
                .expenseDate(LocalDate.now())
                .build();
        expense = expenseRepository.save(expense);

        // Arrange update request
        ExpenseRequest updateRequest = ExpenseRequest.builder()
                .description("Updated description")
                .amount(BigDecimal.valueOf(50.00))
                .categoryId(testCategory.getId())
                .expenseDate(LocalDate.now())
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/expenses/" + expense.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Updated description"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Should delete expense")
    void testDeleteExpense() throws Exception {
        // Create test expense
        Expense expense = Expense.builder()
                .user(testUser)
                .category(testCategory)
                .description("Test")
                .amount(BigDecimal.valueOf(45.99))
                .expenseDate(LocalDate.now())
                .build();
        expense = expenseRepository.save(expense);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/expenses/" + expense.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should return 401 without authentication")
    void testCreateExpense_Unauthorized() throws Exception {
        // Arrange
        ExpenseRequest request = ExpenseRequest.builder()
                .description("Test")
                .amount(BigDecimal.valueOf(45.99))
                .categoryId(1L)
                .expenseDate(LocalDate.now())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
```

---

## Controller Layer Tests

```java
package com.personalfinance.controller;

import com.finance.tracker.dto.expense.*;
import com.finance.tracker.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@DisplayName("Expense Controller Unit Tests")
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @Test
    @WithMockUser
    @DisplayName("Should return expenses page")
    void testGetExpenses() throws Exception {
        // Arrange
        Page<ExpenseResponse> page = new PageImpl<>(java.util.Collections.emptyList());
        when(expenseService.getUserExpenses(any(), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/expenses"))
                .andExpect(status().isOk());
    }
}
```

---

## E2E Testing

```java
package com.personalfinance.e2e;

import com.personalfinance.DailyFinanceTrackerApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    classes = DailyFinanceTrackerApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(locations = "classpath:application-test.yml")
@DisplayName("End-to-End Tests")
class E2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;

    @BeforeEach
    void setUp() {
        // Login to get token
        // Use token for subsequent requests
    }

    @Test
    @DisplayName("Should complete full expense workflow")
    void testFullExpenseWorkflow() {
        // 1. Register user
        // 2. Login
        // 3. Create category
        // 4. Create expense
        // 5. Update expense
        // 6. Get expenses
        // 7. Delete expense
        // 8. Verify deletion
    }
}
```

---

## Test Configuration

**application-test.yml:**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
  data:
    redis:
      port: 6380
```

---

## Test Utilities

```java
package com.personalfinance.util;

import com.finance.tracker.entity.Budget;
import com.finance.tracker.entity.Expense;
import com.finance.tracker.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Test data builder for creating test entities
 */
public class TestDataBuilder {

    public static User.UserBuilder testUserBuilder() {
        return User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashed")
                .fullName("Test User")
                .isActive(true);
    }

    public static Expense.ExpenseBuilder testExpenseBuilder() {
        return Expense.builder()
                .description("Test expense")
                .amount(BigDecimal.valueOf(100.00))
                .currency("USD")
                .expenseDate(LocalDate.now());
    }

    public static Budget.BudgetBuilder testBudgetBuilder() {
        LocalDate now = LocalDate.now();
        return Budget.builder()
                .name("Test Budget")
                .amount(BigDecimal.valueOf(1000.00))
                .period("MONTHLY")
                .startDate(now)
                .endDate(now.plusMonths(1))
                .isActive(true);
    }
}
```

---

## Test Execution

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ExpenseServiceTest

# Run specific test method
mvn test -Dtest=ExpenseServiceTest#testCreateExpense_Success

# Run with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

---

## Code Coverage Goals

- **Overall Coverage:** >80%
- **Critical Paths:** >90%
- **Utility Classes:** >70%

