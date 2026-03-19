package com.finance.tracker.controller;
import com.finance.tracker.dto.TransactionDto;
import com.finance.tracker.service.impl.FileProcessingServices;
import com.finance.tracker.service.impl.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600, allowedHeaders = "*")
public class TransactionController {
    private final TransactionServiceImpl txService;
    private final FileProcessingServices fileProcessingServices;

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

    @PostMapping("/upload-html")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            File temp = File.createTempFile("txn_", ".html");
            file.transferTo(temp);
            fileProcessingServices.importFromHtml(temp);
            return ResponseEntity.ok("File imported successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed: " + e.getMessage());
        }
    }
}
