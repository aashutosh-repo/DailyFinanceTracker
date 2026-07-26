package com.finance.tracker.chatbot.rag.document;

import com.finance.tracker.chatbot.rag.context.FinancialContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class DocumentFactory {
    private final Map<DocumentType,
                FinancialDocumentBuilder> builders;

    public DocumentFactory(
            List<FinancialDocumentBuilder> list) {

        this.builders = list.stream()
                .collect(Collectors.toMap(
                        FinancialDocumentBuilder::getSupportedType,
                        Function.identity()
                ));

    }

    public FinancialDocument create(

            DocumentType type,

            FinancialContext context) {

        return builders.get(type)

                .build(context);

    }
}
