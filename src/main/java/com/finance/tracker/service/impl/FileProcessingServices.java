package com.finance.tracker.service.impl;

import com.finance.tracker.constants.TransactionCategory;
import com.finance.tracker.entity.Transaction;
import com.finance.tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class FileProcessingServices {

    private final TransactionRepository repository;

    public void importFromHtml(File file) throws IOException, ParserConfigurationException, SAXException {
        String content = Files.readString(file.toPath()); // Placeholder for file content reading logic
        String filePath = file.getAbsolutePath();

        //extrct transactions from HTML content
        int startIndex = content.indexOf("<var DATA = '")+10;
        int endIndex = content.indexOf("';</var>");
        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            throw new IOException("Invalid HTML format: DATA variable not found");
        }
        String xmlData = content.substring(startIndex, endIndex);
        //parse xmlData and save transactions to database
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc =builder.parse(new InputSource(new StringReader(xmlData)));


        NodeList transactions = doc.getElementsByTagName("Transaction");
        for (int i = 0; i < transactions.getLength(); i++) {
            Element transactionElement = (Element) transactions.item(i);
            Transaction txn = new Transaction();
            txn.setTypeOfExpense(TransactionCategory.valueOf(transactionElement.getAttribute("Type")));
            txn.setDescription(transactionElement.getAttribute("Description"));
            txn.setDateOfExpense(java.time.LocalDate.parse(transactionElement.getAttribute("Date")));
            txn.setTxnAmount(new java.math.BigDecimal(transactionElement.getAttribute("Amount")));
            txn.setTxnType(transactionElement.getAttribute("TxnType"));
            repository.save(txn);
        }
    }


}
