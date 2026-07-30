package com.finance.tracker.chatbot.tool;

import lombok.Builder;

@Builder
public record ToolResult(

        boolean handled,
        String toolName,
        String data
) { }