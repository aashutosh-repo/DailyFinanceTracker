package com.finance.tracker.controller;
import com.finance.tracker.dto.TransactionDto;
import com.finance.tracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService txService;

    @PostMapping
    public ResponseEntity<TransactionDto> add(@RequestBody TransactionDto dto, Principal p) {
        return ResponseEntity.status(201).body(txService.create(dto, p.getName()));
    }

    @GetMapping
    public List<TransactionDto> list(@RequestParam String from, @RequestParam String to, Principal p) {
        LocalDate f = LocalDate.parse(from);
        LocalDate t = LocalDate.parse(to);
        return txService.findAll(p.getName(), f, t);
    }
}
