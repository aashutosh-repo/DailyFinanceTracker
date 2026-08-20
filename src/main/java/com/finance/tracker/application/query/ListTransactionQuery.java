package com.finance.tracker.application.query;


import com.finance.tracker.domain.transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListTransactionQuery {
    private Long userId;

    //data range Filter
    private LocalDate startDate;
    private LocalDate endDate;

    private TransactionType type;

    //categorization
    private Long categoryId;

    //Type
    private int pageSize;
    private int pageNumber;

    //sorting
    private String sortBy;
    private String sortOrder;

}
