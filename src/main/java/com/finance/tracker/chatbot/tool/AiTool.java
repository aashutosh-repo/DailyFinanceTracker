package com.finance.tracker.chatbot.tool;

public interface AiTool {

    String name();
    boolean supports(String question);
    ToolResult execute(String userId, String question);

}