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
import com.finance.tracker.stock.market.provider.MarketDataClassification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        if (fromDate == null || toDate == null || fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be on or before to");
        }
        if (period < 2 || period > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "period must be between 2 and 200");
        }
        Company company = findCompany(symbol);
        List<MarketPrice> marketPriceList = marketPriceRepository.findByCompanyIdAndPriceDateBetweenOrderByPriceDateAsc(
                company.getId(),
                fromDate,
                toDate
        );
        if(marketPriceList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No market data found for " + symbol);
        }

        validateTechnicalPriceRows(marketPriceList, fromDate, toDate);

        List<PricePoint> pricePoints = marketPriceList.stream().map(this::toPricePoint).toList();
        List<BigDecimal> closes = pricePoints.stream().map(PricePoint::close).toList();

        if (closes.size() < period) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Not enough market data for technical analysis. Requested period=" + period
                    + ", available closes=" + closes.size());
        }

        List<TechnicalIndicatorResponse> indicators = new ArrayList<>();

        if (closes.size() >= period) {
            indicators.add(toResponse(TechnicalAnalysisCalculator.sma(closes, period)));
            indicators.add(toResponse(TechnicalAnalysisCalculator.ema(closes, period)));
        }

        if (closes.size() >= period+1) {
            indicators.add(toResponse(TechnicalAnalysisCalculator.rsi(closes, period)));
        }

        if (pricePoints.size() >= period * 2) {
            indicators.add(toResponse(TechnicalAnalysisCalculator.atr(pricePoints, period)));
        }

        if (pricePoints.size() >= period * 2) {
            indicators.add(toResponse(TechnicalAnalysisCalculator.volumeTrends(pricePoints, period)));
        }

        MacdResonse macd = closes.size() >= 34 ? toResponse(TechnicalAnalysisCalculator.macd(closes))  : null;
        BollingerBandsResponse bollingerBands = closes.size() >= period ? toResponse(TechnicalAnalysisCalculator.bollingerBands(closes,period, BigDecimal.valueOf(2))) : null;
        BigDecimal currentPrice = closes.getLast();
        BigDecimal dailyReturn = closes.size() >= 2 ? percentage(closes.get(closes.size() - 2), currentPrice) : null;
        BigDecimal periodReturn = percentage(closes.getFirst(), currentPrice);
        BigDecimal sma20 = simpleMovingAverage(closes, 20);
        BigDecimal sma50 = simpleMovingAverage(closes, 50);
        BigDecimal sma200 = simpleMovingAverage(closes, 200);
        BigDecimal rsi14 = closes.size() >= 15 ? TechnicalAnalysisCalculator.rsi(closes, 14).value() : null;
        BigDecimal volatility = annualizedVolatility(closes);
        BigDecimal volumeTrend = pricePoints.size() >= 28
            ? TechnicalAnalysisCalculator.volumeTrends(pricePoints, 14).value() : null;
        String trend = sma20 == null ? "UNAVAILABLE"
            : currentPrice.compareTo(sma20) > 0 ? "BULLISH"
            : currentPrice.compareTo(sma20) < 0 ? "BEARISH" : "NEUTRAL";

        return new TechnicalAnalysisResponse(
                company.getSymbol(),
                fromDate,
                toDate,
                marketPriceList.size(),
                indicators,
                macd,
                bollingerBands,
                TechnicalAnalysisCalculator.fiftyTwoWeekHigh(pricePoints),
                TechnicalAnalysisCalculator.fiftyTwoWeekLow(pricePoints),
                currentPrice,
                dailyReturn,
                periodReturn,
                sma20,
                sma50,
                sma200,
                rsi14,
                volatility,
                volumeTrend,
                trend,
                String.join(", ", marketPriceList.stream().map(MarketPrice::getSource).filter(java.util.Objects::nonNull).distinct().toList()),
                MarketDataClassification.fromSources(marketPriceList.stream().map(MarketPrice::getSource).toList()).name(),
                marketPriceList.getFirst().getPriceDate(),
                marketPriceList.getLast().getPriceDate(),
                List.of("No exchange trading calendar is configured; missing dates cannot be classified as missing sessions.")
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

    private void validateTechnicalPriceRows(List<MarketPrice> prices, LocalDate fromDate, LocalDate toDate) {
        LocalDate previousDate = null;
        for (MarketPrice price : prices) {
            if (price == null || price.getPriceDate() == null || price.getPriceDate().isBefore(fromDate)
                    || price.getPriceDate().isAfter(toDate) || price.getClosePrice() == null
                    || price.getHighPrice() == null || price.getLowPrice() == null || price.getVolume() == null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Stored market data is incomplete or outside the requested range");
            }
            if (previousDate != null && !price.getPriceDate().isAfter(previousDate)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Stored market dates are duplicated or not chronological");
            }
            if (price.getClosePrice().compareTo(BigDecimal.ZERO) <= 0
                    || price.getHighPrice().compareTo(BigDecimal.ZERO) <= 0
                    || price.getLowPrice().compareTo(BigDecimal.ZERO) <= 0
                    || price.getHighPrice().compareTo(price.getClosePrice()) < 0
                    || price.getLowPrice().compareTo(price.getClosePrice()) > 0
                    || price.getVolume() < 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Stored market prices are invalid");
            }
            previousDate = price.getPriceDate();
        }
    }

    private BigDecimal simpleMovingAverage(List<BigDecimal> closes, int period) {
        return closes.size() >= period ? TechnicalAnalysisCalculator.sma(closes, period).value() : null;
    }

    private BigDecimal percentage(BigDecimal start, BigDecimal end) {
        if (start == null || start.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return end.subtract(start).divide(start, 4, RoundingMode.HALF_UP)
                .movePointRight(2).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal annualizedVolatility(List<BigDecimal> closes) {
        if (closes.size() < 3) {
            return null;
        }
        List<BigDecimal> returns = new ArrayList<>();
        for (int index = 1; index < closes.size(); index++) {
            returns.add(closes.get(index).subtract(closes.get(index - 1))
                    .divide(closes.get(index - 1), 8, RoundingMode.HALF_UP));
        }
        BigDecimal mean = returns.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), 8, RoundingMode.HALF_UP);
        BigDecimal variance = returns.stream()
                .map(value -> value.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size() - 1), 8, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue() * 252) * 100)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Company findCompany(String symbol) {
        return companyRepository.findBySymbol(symbol)
                .orElseThrow(() -> new IllegalArgumentException("Company not Found: " + symbol));
    }

}
