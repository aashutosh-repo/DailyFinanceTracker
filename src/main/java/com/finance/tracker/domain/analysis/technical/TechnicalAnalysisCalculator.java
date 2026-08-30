package com.finance.tracker.domain.analysis.technical;

import com.finance.tracker.controller.HistoryController;
import com.finance.tracker.domain.analysis.Signal;

import javax.swing.event.ListDataEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TechnicalAnalysisCalculator {
    private static final int SCALE= 4;
    private TechnicalAnalysisCalculator(){}

    public static TechnicalIndicator sma(List<BigDecimal> closes, int period) {
        validatePeriod(closes, period);
        BigDecimal sum = closes.subList(closes.size()-period, closes.size()).stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal value = sum.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
        return indicator("SMA", value, Signal.NEUTRAL);
    }

    public static TechnicalIndicator ema(List<BigDecimal> closes, int period) {
        validatePeriod(closes, period);
        BigDecimal multiplier = BigDecimal.valueOf(2.0/(period+1));
        BigDecimal emaValue = sma(closes.subList(0, period), period).value();

        for (int index = period; index < closes.size(); index++) {
            emaValue = closes.get(index).subtract(emaValue).multiply(multiplier).add(emaValue);
        }
        return indicator("EMA", emaValue.setScale(SCALE, RoundingMode.HALF_UP), Signal.NEUTRAL);
    }

    public static TechnicalIndicator rsi(List<BigDecimal> closes, int period) {
        validatePeriod(closes, period);
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal losses = BigDecimal.ZERO;
        int smartIndex = closes.size() - period;

        for (int index= smartIndex; index<closes.size(); index++) {
            BigDecimal change = closes.get(index).subtract(closes.get(index-1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains = gains.add(change);
            } else {
                losses = losses.add(change);
            }
        }

        BigDecimal averageGains = gains.divide(BigDecimal.valueOf(period), SCALE+2, RoundingMode.HALF_UP);
        BigDecimal averageLosses = losses.divide(BigDecimal.valueOf(period), SCALE+2, RoundingMode.HALF_UP);
        BigDecimal rsiValue = averageLosses.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.valueOf(100): BigDecimal.valueOf(100).subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(averageGains.divide(averageLosses, SCALE+2, RoundingMode.HALF_UP)), SCALE, RoundingMode.HALF_UP));

        Signal signal = Signal.NEUTRAL;
        if (rsiValue.compareTo(BigDecimal.valueOf(70)) > 0) {
            signal = Signal.BEARISH;
        } else if (rsiValue.compareTo(BigDecimal.valueOf(30)) < 0) {
            signal = Signal.BULLISH;
        }

        return indicator("RSI", rsiValue.setScale(SCALE, RoundingMode.HALF_UP), signal);
    }

    public static MacdResult macd(List<BigDecimal> closes) {
        validatePeriod(closes, 35);
        List<BigDecimal> macdValues = new ArrayList<>();

        for (int index =0; index <= closes.size(); index++) {
            List<BigDecimal> partial = closes.subList(0, index);
            BigDecimal fast = ema(partial, 12).value();
            BigDecimal slow = ema(partial, 26).value();
            macdValues.add(fast.subtract(slow));
        }

        BigDecimal macdLine = macdValues.getLast().setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal signalLine = ema(macdValues, 9).value();
        BigDecimal histogram = macdLine.subtract(signalLine).setScale(SCALE, RoundingMode.HALF_UP);
        Signal signal= histogram.compareTo(BigDecimal.ZERO) > 0 ? Signal.BULLISH : histogram.compareTo(BigDecimal.ZERO) < 0 ? Signal.BEARISH : Signal.NEUTRAL;
        return new MacdResult(macdLine, signalLine, histogram, signal);
    }

    public static BollingerBands bollingerBands(List<BigDecimal> closes, int period, BigDecimal standardDeviationMultiplier) {
        TechnicalIndicator middle = sma(closes, period);
        List<BigDecimal> recentCLoses = closes.subList(closes.size()-period, closes.size());
        BigDecimal varience = recentCLoses.stream()
                .map(close -> close.subtract(middle.value()).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(period), SCALE+2, RoundingMode.HALF_UP);
        BigDecimal standardDeviation = BigDecimal.valueOf(Math.sqrt(varience.doubleValue())).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal offSet = standardDeviation.multiply(standardDeviationMultiplier).setScale(SCALE, RoundingMode.HALF_UP);
        return new BollingerBands(middle.value().subtract(offSet), middle.value(), middle.value().add(offSet));
    }

    public static TechnicalIndicator atr(List<PricePoint> prices, int period) {
        validatePricePeriod(prices, period+1);
        List<PricePoint> orderedPrice = prices.stream().sorted(Comparator.comparing(PricePoint::date)).toList();
        BigDecimal trueRangeSum = BigDecimal.ZERO;

        for (int index = orderedPrice.size()-period; index < orderedPrice.size(); index++) {
            PricePoint current = orderedPrice.get(index);
            PricePoint previous = orderedPrice.get(index-1);
            BigDecimal highLow = current.high().subtract(current.low().abs());
            BigDecimal highClose = current.high().subtract(previous.close().abs());
            BigDecimal lowClose = current.low().subtract(current.close().abs());
            trueRangeSum = trueRangeSum.add(highLow.max(lowClose));
        }

        BigDecimal value = trueRangeSum.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
        return indicator("ATR", value, Signal.NEUTRAL);

    }

    public static TechnicalIndicator volumeTrends(List<PricePoint> prices, int period) {
        validatePricePeriod(prices, period);

        List<PricePoint> orderedPrices = prices.stream().sorted(Comparator.comparing(PricePoint::date)).toList();
        BigDecimal recentAverage = averageVolume(orderedPrices.subList(orderedPrices.size()-period, orderedPrices.size()));
        BigDecimal previousAverage = averageVolume(orderedPrices.subList(orderedPrices.size()-(period*2), orderedPrices.size()-period));
        BigDecimal change = recentAverage.subtract(previousAverage).divide(previousAverage, SCALE, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        Signal signal = change.compareTo(BigDecimal.TEN) > 0 ? Signal.BULLISH : change.compareTo(BigDecimal.TEN.negate()) < 0 ? Signal.BEARISH : Signal.NEUTRAL;
        return indicator("VOLUME_TREND", change.setScale(SCALE, RoundingMode.HALF_UP),signal);
    }

    public static BigDecimal fiftyTwoWeekHigh(List<PricePoint> prices) {
        return prices.stream().map(PricePoint::high).max(BigDecimal::compareTo).orElseThrow();
    }

   public static BigDecimal fiftyTwoWeekLow(List<PricePoint> prices) {
        return prices.stream().map(PricePoint::low).max(BigDecimal::compareTo).orElseThrow();
    }

    private static BigDecimal averageVolume(List<PricePoint> prices) {
        BigDecimal totalVolume = prices.stream().map(price -> BigDecimal.valueOf(price.volume())).reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalVolume.divide(BigDecimal.valueOf(prices.size()), SCALE, RoundingMode.HALF_UP);
    }

    private static void validatePricePeriod(List<PricePoint> values, int period) {
        if (period <=0 || values == null || values.size() < period) {
            throw new IllegalArgumentException("Not Enough price points for period");
        }
    }

    private static TechnicalIndicator indicator(String name, BigDecimal value, Signal signal) {
        return new TechnicalIndicator(name, value, signal, LocalDateTime.now(), "market-data");
    }

    private static void validatePeriod(List<BigDecimal> values, int period) {
        if (period <= 0 || values == null || values.size() < period) {
            throw new IllegalArgumentException("Not Enough value for period");
        }
    }
}
