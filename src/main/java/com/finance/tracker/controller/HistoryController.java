package com.finance.tracker.controller;

import com.finance.tracker.chatbot.memory.Conversation;
import com.finance.tracker.chatbot.memory.ConversationRepository;
import com.finance.tracker.entity.User;
import com.finance.tracker.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public HistoryController(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getHistory() {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) return ResponseEntity.status(401).body("Unauthorized");

        User user = userRepository.findByEmail(principal).orElse(null);
        if (user == null) return ResponseEntity.status(404).body("User not found");

        List<Conversation> list = conversationRepository.findByUserId(user.getId().toString());
        return ResponseEntity.ok(list);
    }
}
