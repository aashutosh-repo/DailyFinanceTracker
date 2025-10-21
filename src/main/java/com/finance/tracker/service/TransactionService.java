package com.finance.tracker.service;
import com.finance.tracker.dto.TransactionDto;
import com.finance.tracker.entity.Transaction;
import com.finance.tracker.entity.User;
import com.finance.tracker.repository.TransactionRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository txRepo;
    private final UserRepository userRepo;

    public TransactionDto create(TransactionDto dto, String userEmail) {
        User user = userRepo.findByEmail(userEmail).orElseThrow();
        Transaction t = Transaction.builder()
                .user(user)
                .amount(dto.getAmount())
                .txnType(dto.getTxnType())
                .txnDate(dto.getTxnDate())
                .description(dto.getDescription())
                .build();
        txRepo.save(t);
        dto.setId(t.getId());
        dto.setUserId(user.getId());
        return dto;
    }

    public List<TransactionDto> findAll(String userEmail, LocalDate from, LocalDate to) {
        User user = userRepo.findByEmail(userEmail).orElseThrow();
        List<Transaction> list = txRepo.findByUserIdAndTxnDateBetween(user.getId(), from, to);
        return list.stream().map(t -> {
            TransactionDto d = new TransactionDto();
            d.setId(t.getId());
            d.setUserId(user.getId());
            d.setAmount(t.getAmount());
            d.setTxnType(t.getTxnType());
            d.setTxnDate(t.getTxnDate());
            d.setDescription(t.getDescription());
            return d;
        }).collect(Collectors.toList());
    }
}
