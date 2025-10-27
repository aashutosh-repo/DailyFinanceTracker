package com.finance.tracker.service.impl;

import com.finance.tracker.constants.ExpenseType;
import com.finance.tracker.dto.TransactionDto;
import com.finance.tracker.entity.Transaction;
import com.finance.tracker.entity.User;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.mapper.TransactionMapper;
import com.finance.tracker.repository.TransactionRepository;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

//    public TransactionDto create(TransactionDto dto, String userEmail) {
//        User user = userRepo.findByEmail(userEmail).orElseThrow();
//        Transaction t = Transaction.builder()
//                .user(user)
//                .txnAmount(dto.getTxnAmount())
//                .txnType(dto.getTxnType())
//                .dateOfExpense(dto.getDateOfExpense())
//                .description(dto.getDescription())
//                .build();
//        txRepo.save(t);
//        dto.setId(t.getId());
//        dto.setUserId(user.getId());
//        return dto;
//    }


    @Override
    public TransactionDto addExpense(TransactionDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + dto.getUserId()));

        Transaction expense = TransactionMapper.toEntity(dto, user);
        if(expense.getTypeOfExpense()==null){
            expense.setTypeOfExpense(ExpenseType.OTHER);
        }

        Transaction saved = transactionRepository.save(expense);
        dto.setId(saved.getId());
        return dto;
    }

    @Override
    public List<TransactionDto> getAllExpensesByUser(Long userId) {
        return transactionRepository.findByUserId(userId)
                .stream()
                .map(TransactionMapper::toDto)
                .toList();
    }

    @Override
    public TransactionDto getExpenseById(Long id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        return TransactionMapper.toDto(t);
    }

    @Override
    public void deleteExpense(Long id) {
        transactionRepository.deleteById(id);
    }
}
