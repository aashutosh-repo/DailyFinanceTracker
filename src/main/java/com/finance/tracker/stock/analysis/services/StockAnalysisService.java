package com.finance.tracker.stock.analysis.services;

import com.finance.tracker.domain.analysis.fundamental.dto.FinancialData;
import com.finance.tracker.domain.analysis.scoring.ScoreBreakDown;
import com.finance.tracker.domain.analysis.scoring.StockScoringEngine;
import com.finance.tracker.domain.analysis.technical.*;
import com.finance.tracker.domain.analysis.valuation.DcfInput;
import com.finance.tracker.domain.analysis.valuation.DcfValuationCalculator;
import com.finance.tracker.domain.analysis.valuation.DcfValuationResult;
import com.finance.tracker.stock.analysis.dto.*;
import com.finance.tracker.stock.company.Company;
import com.finance.tracker.stock.company.CompanyRepository;
import com.finance.tracker.stock.fudamentals.services.FundamentalsService;
import com.finance.tracker.stock.market.MarketPriceRepository;
import com.finance.tracker.stock.market.entity.MarketPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockAnalysisService {
    private final CompanyRepository companyRepository;
    private final MarketPriceRepository marketPriceRepository;
    private final FundamentalsService fundamentalsService;

    public TechnicalAnalysisResponse getTechnicalAnalysis(String symbol, LocalDate fromDate, LocalDate toDate, int period) {
        Company company = findCompany(symbol);
        List<MarketPrice> marketPriceList = marketPriceRepository.findByCompanyIdAndPriceDateBetweenOrderByPriceDateAsc(
                company.getId(),
                fromDate,
                toDate
        );
        if(marketPriceList.isEmpty()) {
            throw new IllegalArgumentException("No Market Data Found" + symbol);
        }

        List<PricePoint> pricePoints = marketPriceList.stream().map(this::toPricePoint).toList();
        List<BigDecimal> closes = pricePoints.stream().map(PricePoint::close).toList();

        List<TechnicalIndicatorResponse> indicators = new ArrayList<>();

        if (closes.size() >= period) {
            indicators.add(toResponse(TechnicalAnalysisCalculator.sma(closes, period)));
            indicators.add(toResponse(TechnicalAnalysisCalculator.ema(closes, period)));
        }

        if (closes.size() >= period+1) {
            indicators.add(toResponse(TechnicalAnalysisCalculator.rsi(closes, period)));
        }

        if (pricePoints.size() >= period+1) {
            indicators.add(toResponse(TechnicalAnalysisCalculator.atr(pricePoints, period)));
        }

        if (pricePoints.size() >= period+2) {
            indicators.add(toResponse(TechnicalAnalysisCalculator.volumeTrends(pricePoints, period)));
        }

        MacdResonse macd = closes.size() >= 35 ? toResponse(TechnicalAnalysisCalculator.macd(closes))  : null;
        BollingerBandsResponse bollingerBands = closes.size() >= period ? toResponse(TechnicalAnalysisCalculator.bollingerBands(closes,period, BigDecimal.valueOf(2))) : null;

        return new TechnicalAnalysisResponse(
                company.getSymbol(),
                fromDate,
                toDate,
                marketPriceList.size(),
                indicators,
                macd,
                bollingerBands,
                TechnicalAnalysisCalculator.fiftyTwoWeekHigh(pricePoints),
                TechnicalAnalysisCalculator.fiftyTwoWeekLow(pricePoints)
        );
    }

    public DcfValuationResponse calculateDcf(String symbol, DcfValuationRequest request) {
        Company company = findCompany(symbol);
        DcfInput input = new DcfInput(
                request.startingRevenue(),
                request.revenueGrowth(),
                request.ebitMargin(),
                request.taxRate(),
                request.wacc(),
                request.capexPercentageOfRevenue(),
                request.workingCapitalPercentOfRevenue(),
                request.terminalGrowthRate(),
                request.shareOutstanding(),
                request.projectionYear()
        );

        DcfValuationResult result = DcfValuationCalculator.calculate(input);
        return new DcfValuationResponse(
                company.getSymbol(),
                result.enterpriseValue(),
                result.intrinsicValuePerShare(),
                result.projectedFreeCashFlow()
        );
    }

    public DcfValuationResponse calculateDcfFromFundamentals(String symbol, DcfFromFundamentalsRequest request) {
        Company company = findCompany(symbol);
        FinancialData financialData = fundamentalsService.getFinancialData(symbol);

        DcfInput input = new DcfInput(
                required(financialData.latestRevenue(), "latest revenue"),
                decimalRate(resolve(request.revenueGrowthRateOverride(), financialData.revenueGrowthRate())),
                decimalRate(resolve(request.ebitMarginOverride(), margin(financialData.latestEbit(), financialData.latestRevenue()))),
                request.taxRate(),
                request.wacc(),
                decimalRate(resolve(request.capexPercentOfRevenueOverride(), margin(financialData.latestCapEx(), financialData.latestRevenue()))),
                decimalRate(resolve(request.workingCapitalPercentOfRevenueOverride(), BigDecimal.ZERO)),
                request.terminalGrowthRate(),
                request.shareOutstanding(),
                request.projectionYear()
        );

        DcfValuationResult result = DcfValuationCalculator.calculate(input);

        return new DcfValuationResponse(
                company.getSymbol(),
                result.enterpriseValue(),
                result.intrinsicValuePerShare(),
                result.projectedFreeCashFlow()
        );

    }

    private BigDecimal margin(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.divide(denominator, 8, RoundingMode.HALF_UP);
    }

    private BigDecimal decimalRate(BigDecimal rate) {
        BigDecimal requiredRate = required(rate, "derived or overridden rate");
        return requiredRate.abs().compareTo(BigDecimal.ONE) > 0
                ? requiredRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)
                : requiredRate;

    }

    private BigDecimal resolve(BigDecimal override, BigDecimal derivedValue) {
        return override != null ? override : derivedValue;
    }

    private BigDecimal required(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException("DCF from fundamental requires "+ fieldName);
        }
        return value;
    }

    public StockScoreResponse calculateScore(String symbol, StockScoreRequest request) {
        Company company = findCompany(symbol);
        ScoreBreakDown score = StockScoringEngine.score(
                request.fundamentalScore(),
                request.growthScore(),
                request.valuationScore(),
                request.financialHealthScore(),
                request.technicalScore(),
                request.sentimentScore(),
                request.riskScore(),
                null
        );

        return new StockScoreResponse(
                company.getSymbol(),
                score.overallScore(),
                score.fundamentalScore(),
                score.growthScore(),
                score.valuationScore(),
                score.financialHealthScore(),
                score.technicalScore(),
                score.sentimentScore(),
                score.riskScore()
        );
    }

    private TechnicalIndicatorResponse toResponse(TechnicalIndicator indicator) {
        return new TechnicalIndicatorResponse(
                indicator.indicator(),
                indicator.value(),
                indicator.signal(),
                indicator.calculatedAt(),
                indicator.source()
        );
    }

    private MacdResonse toResponse(MacdResult macdResult) {
        return new MacdResonse(
                macdResult.macdLine(),
                macdResult.signalLine(),
                macdResult.histogram(),
                macdResult.signal()
        );
    }

    private BollingerBandsResponse toResponse(BollingerBands bollingerBands) {
        return new BollingerBandsResponse(
                bollingerBands.lowerBand(),
                bollingerBands.middleBand(),
                bollingerBands.upperBand()
        );
    }

    private PricePoint toPricePoint(MarketPrice marketPrice) {
        return new PricePoint(
                marketPrice.getPriceDate(),
                marketPrice.getHighPrice(),
                marketPrice.getLowPrice(),
                marketPrice.getClosePrice(),
                marketPrice.getVolume() == null ? 0L : marketPrice.getVolume()
        );
    }

    private Company findCompany(String symbol) {
        return companyRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Company not Found: " + symbol));
    }

}
