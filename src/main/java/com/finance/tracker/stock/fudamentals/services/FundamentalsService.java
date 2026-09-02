package com.finance.tracker.stock.fudamentals.services;

import com.finance.tracker.stock.fudamentals.repositoty.FinancialStatementRepository;
import com.finance.tracker.domain.analysis.fundamental.dto.FinancialData;
import com.finance.tracker.domain.analysis.fundamental.entity.FinancialStatement;
import com.finance.tracker.stock.company.Company;
import com.finance.tracker.stock.company.CompanyRepository;
import com.finance.tracker.stock.fudamentals.dto.FinancialStatementRequest;
import com.finance.tracker.stock.fudamentals.dto.FinancialStatementResponse;
import com.finance.tracker.stock.fudamentals.dto.FundamentalsOverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FundamentalsService {

    private final FinancialStatementRepository financialStatementRepository;
    private final CompanyRepository companyRepository;

    public FundamentalsOverviewResponse getFundamentalsOverview(String symbol) {
        Company company = findCompany(symbol);
        List<FinancialStatement> statementList = financialStatementRepository.findLastNYear(company.getId(), 5);

        if (statementList.isEmpty()) {
            throw new IllegalArgumentException("No financial data available for: "+ symbol);
        }

        FinancialStatement latest = statementList.getFirst();
        FinancialData data = calculateFinancialData(statementList);

        BigDecimal margin = data.latestOperatingCashFlow() != null && data.latestCapEx() != null
                ? data.latestOperatingCashFlow().subtract(data.latestCapEx()) : null;
        return new FundamentalsOverviewResponse(
                company.getSymbol(),
                latest.getReportDate(),
                data.latestRevenue(),
                calculateYoYGrowth(statementList, FinancialStatement::getRevenue),
                data.revenueGrowthRate(),
                data.latestEbit(),
                calculateMargin(data.latestEbit(), data.latestRevenue()),
                data.latestNetIncome(),
                calculateMargin(data.latestNetIncome(), data.latestRevenue()),
                data.latestOperatingCashFlow(),
                margin,
                calculateMargin(
                        margin,
                        data.latestRevenue()
                ),
                data.latestTotalDebt(),
                data.latestTotalEquity(),
                calculateRatio(data.latestTotalDebt(), data.latestTotalEquity()),
                calculateCurrentRatio(latest),
                calculateRoe(data.latestNetIncome(), data.latestTotalEquity()),
                calculateRoa(data.latestNetIncome(), latest.getTotalAssets()),
                data.yearOfData(),
                statementList.stream().map(this::mapToResponse).toList()
        );
    }

    @Transactional
    public FinancialStatementResponse saveFinancialStatement(String symbol, FinancialStatementRequest request) {
        Company company = findCompany(symbol);

        Optional<FinancialStatement> existing = financialStatementRepository.findByCompanyIdAndFiscalYearAndFiscalQuarter(
                company.getId(), request.fiscalYear(), request.fiscalQuarter()
        );

        FinancialStatement statement = existing.orElseGet(() -> FinancialStatement.builder()
                .company(company)
                .build()
        );
        statement.setFiscalYear(request.fiscalYear());
        statement.setFiscalQuarter(request.fiscalQuarter());
        statement.setReportDate(request.reportDate());
        statement.setRevenue(request.revenue());
        statement.setOperatingIncome(request.operatingIncome());
        statement.setEbit(request.ebit());
        statement.setNetIncome(request.netIncome());
        statement.setOperatingCashFlow(request.operatingCashFlow());
        statement.setCapitalExpenditures(request.capitalExpenditures());
        statement.setFreeCashFlow(request.freeCashFlow());
        statement.setTotalAssets(request.totalAssets());
        statement.setTotalLiabilities(request.totalLiability());
        statement.setTotalEquity(request.totalEquity());
        statement.setTotalDebt(request.totalDebt());
        statement.setCash(request.cash());
        statement.setWorkingCapital(request.workingCapital());
        statement.setSource(request.source());

        FinancialStatement saved = financialStatementRepository.save(statement);
        return mapToResponse(saved);
    }

    public FinancialData getFinancialData(String symbol) {
        Company company = findCompany(symbol);

        List<FinancialStatement> statements = financialStatementRepository.findLastNYear(company.getId(), 5);

        if(statements.isEmpty()) {
            throw new IllegalArgumentException("No financial data available for "+ symbol);
        }

        return calculateFinancialData(statements);
    }

    private FinancialData calculateFinancialData(List<FinancialStatement> statements) {
        if (statements.isEmpty()) {
            throw new IllegalArgumentException("No statement provided");
        }

        FinancialStatement latest = statements.getFirst();
        BigDecimal revenueGrowth = calculateCAGR(statements, FinancialStatement::getRevenue);
        BigDecimal niGrowth = calculateCAGR(statements, FinancialStatement::getNetIncome);
        BigDecimal ocfGrowth = calculateCAGR(statements, FinancialStatement::getOperatingCashFlow);

        return new FinancialData(
                latest.getRevenue(),
                latest.getEbit(),
                latest.getNetIncome(),
                latest.getOperatingCashFlow(),
                latest.getCapitalExpenditures(),
                latest.getTotalDebt(),
                latest.getTotalEquity(),
                latest.getCash(),
                revenueGrowth,
                niGrowth,
                ocfGrowth,
                statements.size()
        );

    }

    private FinancialStatementResponse mapToResponse(FinancialStatement statement) {
        return new FinancialStatementResponse(
                statement.getCompany().getSymbol(),
                statement.getFiscalYear(),
                statement.getFiscalQuarter(),
                statement.getReportDate(),
                statement.getRevenue(),
                statement.getOperatingIncome(),
                statement.getEbit(),
                statement.getNetIncome(),
                statement.getOperatingCashFlow(),
                statement.getCapitalExpenditures(),
                statement.getFreeCashFlow(),
                statement.getTotalAssets(),
                statement.getTotalLiabilities(),
                statement.getTotalEquity(),
                statement.getTotalDebt(),
                statement.getCash(),
                statement.getWorkingCapital(),
                statement.getSource()
        );
    }

    private BigDecimal calculateRoe(BigDecimal netIncome, BigDecimal equity) {
        if (netIncome == null || equity == null || equity.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return netIncome.divide(equity, 4, RoundingMode.HALF_UP);
    }
    private BigDecimal calculateRoa(BigDecimal netIncome, BigDecimal asset) {
        if (netIncome == null || asset == null || asset.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return netIncome.divide(asset, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateCurrentRatio(FinancialStatement statement) {
        if (statement.getWorkingCapital() != null) {
            return statement.getWorkingCapital().compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        return null;
    }

    private BigDecimal calculateRatio(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.divide(denominator,4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMargin(BigDecimal amount, BigDecimal revenue) {
        if (amount == null || revenue == null || revenue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return amount.divide(revenue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    private BigDecimal calculateYoYGrowth(List<FinancialStatement> statementList, Function<FinancialStatement, BigDecimal> extractor) {
        if (statementList.size() < 2) {
            return BigDecimal.ZERO;
        }
        BigDecimal current = extractor.apply(statementList.getFirst());
        BigDecimal previous = extractor.apply(statementList.getLast());

        if (current == null || previous == null || previous.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    private BigDecimal calculateCAGR(List<FinancialStatement> statements, Function<FinancialStatement, BigDecimal> extractor)  {
        if(statements.size() <2) {
            return BigDecimal.ZERO;
        }

        BigDecimal endValue = extractor.apply(statements.getFirst());
        BigDecimal startValue = extractor.apply(statements.getLast());

        if (endValue == null || startValue ==null || startValue.compareTo(BigDecimal.ZERO) <=0) {
            return BigDecimal.ZERO;
        }

        int period = statements.size() -1;
        BigDecimal ratio = endValue.divide(startValue, 10, RoundingMode.HALF_UP);
        BigDecimal cagr = ratio.pow(1).divide(BigDecimal.valueOf(period),4, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE);

        return cagr.multiply(new BigDecimal("100"));
    }

    private Company findCompany(String symbol) {
        return companyRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Company not Found: " + symbol));
    }

}

