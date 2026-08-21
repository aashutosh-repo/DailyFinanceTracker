package com.finance.tracker.application.query;


import com.finance.tracker.application.dto.TransactionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListTransactionQueryResult {

    private List<TransactionDTO> transactions;
    private Long totalCount;
    private int pageSize;
    private int pageNumber;
    private int totalPages;
}
