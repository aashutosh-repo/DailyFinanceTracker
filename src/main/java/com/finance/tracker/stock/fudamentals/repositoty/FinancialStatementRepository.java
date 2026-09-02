package com.finance.tracker.stock.fudamentals.repositoty;

import com.finance.tracker.domain.analysis.fundamental.entity.FinancialStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, UUID> {

    List<FinancialStatement> findByCompanyIdOrderByReportDateDesc(UUID companyId);

    Optional<FinancialStatement> findByCompanyIdAndFiscalYearAndFiscalQuarter(
            UUID companyId,
            int fiscalYear,
            int fiscalQuarter
    );

    @Query("SELECT f from FinancialStatement f " +
    "WHERE f.company.id = :companyId " +
    "ORDER BY f.reportDate DESC LIMIT 4"
    )
    List<FinancialStatement> findLatestFourQuarters(UUID companyId);

    @Query("SELECT f from FinancialStatement f " +
    "WHERE f.company.id = :companyId " +
    "AND f.fiscalQuarter = 4 " +
    "ORDER BY f.fiscalYear DESC LIMIT :years"
    )
    List<FinancialStatement> findLastNYear(UUID companyId, int years);

}
