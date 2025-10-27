package com.finance.tracker.controller;
import com.finance.tracker.dto.TransactionDto;
import com.finance.tracker.service.impl.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionServiceImpl txService;

    @PostMapping
    public ResponseEntity<TransactionDto> addExpense(@RequestBody TransactionDto dto) {
        return ResponseEntity.ok(txService.addExpense(dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDto>> getExpensesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(txService.getAllExpensesByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getExpense(@PathVariable Long id) {
        return ResponseEntity.ok(txService.getExpenseById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        txService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
