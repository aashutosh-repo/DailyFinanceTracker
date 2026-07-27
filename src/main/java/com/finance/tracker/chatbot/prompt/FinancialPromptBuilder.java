package com.finance.tracker.chatbot.prompt;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FinancialPromptBuilder implements PromptBuilder {

    @Override
    public String buildPrompt(String question,
                              List<Document> documents) {

        StringBuilder context = new StringBuilder();

        for (Document document : documents) {

            context.append(document.getText())
                    .append("\n\n");
        }

        return """
                You are an AI Financial Budget Assistant.

                Your responsibilities:

                - Answer ONLY using the financial information provided below.
                - If the answer cannot be found in the provided context,
                  politely say that the information is unavailable.
                - Never make up numbers.
                - Never provide investment advice.
                - Explain financial information clearly.

                =============================
                FINANCIAL CONTEXT
                =============================

                %s

                =============================
                USER QUESTION
                =============================

                %s

                Provide a professional and easy-to-understand answer.
                """.formatted(context, question);
    }
}
