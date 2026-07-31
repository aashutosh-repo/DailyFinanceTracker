package com.finance.tracker.chatbot.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AiToolRegistry {
    private final List<AiTool> tools;
    public List<AiTool> getTools() {
        return tools;
    }

}
