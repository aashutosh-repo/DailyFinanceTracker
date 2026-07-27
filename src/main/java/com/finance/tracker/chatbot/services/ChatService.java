package com.finance.tracker.chatbot.services;

import com.finance.tracker.dto.chatbot.ChatResponse;

public interface ChatService {

    ChatResponse chat(String userId,
                      String question);

}