package com.finance.tracker.chatbot.memory;

import com.finance.tracker.chatbot.constants.MessageRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversation_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    private MessageRole role;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;

}