package com.finance.tracker.chatbot.prompt;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FinancialPromptBuilder implements PromptBuilder {

    @Override
    public String buildPrompt(List<Document> documents) {

        if(documents.isEmpty()){
            return "No Documents found in [FinancialPromptBuilder]";
        }
        StringBuilder context = new StringBuilder();

        for (Document document : documents) {

            context.append(document.getText())
                    .append("\n\n");
        }

        return context.toString();
    }
}
