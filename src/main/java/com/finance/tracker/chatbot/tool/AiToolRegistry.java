package com.finance.tracker.chatbot.tool;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
@RequiredArgsConstructor
public class AiToolRegistry {
    private final List<AiTool> tools;

}
