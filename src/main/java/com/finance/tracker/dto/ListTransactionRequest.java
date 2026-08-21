package com.finance.tracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListTransactionRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private Long categoryId;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    @Builder.Default
    private Integer pageSize = 20;

    @Min(value = 0, message = "Page Number cannot be negative")
    @Builder.Default
    private Integer pageNumber = 0;

    @Builder.Default
    private String sortBy = "transactionDate";
    @Builder.Default
    private String sortOrder = "DESC";
}
