package com.finance.tracker.domain.analysis.technical;

import com.finance.tracker.domain.analysis.Signal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TechnicalAnalysisCalculator {
    private static final int SCALE= 4;
    private static final int WORKING_SCALE = 12;
    private static final int MACD_FAST_PERIOD = 12;
    private static final int MACD_SLOW_PERIOD = 26;
    private static final int MACD_SIGNAL_PERIOD = 9;
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
        BigDecimal emaValue = emaSeries(closes, period).getLast();
        return indicator("EMA", emaValue.setScale(SCALE, RoundingMode.HALF_UP), Signal.NEUTRAL);
    }

    public static TechnicalIndicator rsi(List<BigDecimal> closes, int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("RSI period must be positive");
        }
        validatePeriod(closes, period + 1);
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal losses = BigDecimal.ZERO;
        int firstChangeIndex = closes.size() - period;

        for (int index = firstChangeIndex; index < closes.size(); index++) {
            BigDecimal change = closes.get(index).subtract(closes.get(index-1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains = gains.add(change);
            } else if (change.compareTo(BigDecimal.ZERO) < 0) {
                losses = losses.add(change.abs());
            }
        }

        BigDecimal totalMovement = gains.add(losses);
        BigDecimal rsiValue = totalMovement.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.valueOf(50)
                : gains.multiply(BigDecimal.valueOf(100))
                        .divide(totalMovement, SCALE, RoundingMode.HALF_UP);

        Signal signal = Signal.NEUTRAL;
        if (rsiValue.compareTo(BigDecimal.valueOf(70)) > 0) {
            signal = Signal.BEARISH;
        } else if (rsiValue.compareTo(BigDecimal.valueOf(30)) < 0) {
            signal = Signal.BULLISH;
        }

        return indicator("RSI", rsiValue.setScale(SCALE, RoundingMode.HALF_UP), signal);
    }

    public static MacdResult macd(List<BigDecimal> closes) {
        validatePeriod(closes, MACD_SLOW_PERIOD + MACD_SIGNAL_PERIOD - 1);
        List<BigDecimal> macdValues = new ArrayList<>();

        List<BigDecimal> fastEma = emaSeries(closes, MACD_FAST_PERIOD);
        List<BigDecimal> slowEma = emaSeries(closes, MACD_SLOW_PERIOD);
        for (int index = MACD_SLOW_PERIOD - 1; index < closes.size(); index++) {
            macdValues.add(fastEma.get(index).subtract(slowEma.get(index)).setScale(SCALE, RoundingMode.HALF_UP));
        }

        BigDecimal macdLine = macdValues.getLast().setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal signalLine = emaSeries(macdValues, MACD_SIGNAL_PERIOD).getLast();
        BigDecimal histogram = macdLine.subtract(signalLine).setScale(SCALE, RoundingMode.HALF_UP);
        Signal signal= histogram.compareTo(BigDecimal.ZERO) > 0 ? Signal.BULLISH : histogram.compareTo(BigDecimal.ZERO) < 0 ? Signal.BEARISH : Signal.NEUTRAL;
        return new MacdResult(macdLine, signalLine, histogram, signal);
    }

    public static BollingerBands bollingerBands(List<BigDecimal> closes, int period, BigDecimal standardDeviationMultiplier) {
        if (standardDeviationMultiplier == null || standardDeviationMultiplier.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Standard deviation multiplier must be non-negative");
        }
        TechnicalIndicator middle = sma(closes, period);
        List<BigDecimal> recentCLoses = closes.subList(closes.size()-period, closes.size());
        BigDecimal varience = recentCLoses.stream()
                .map(close -> close.subtract(middle.value()).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(period), SCALE+2, RoundingMode.HALF_UP);
        BigDecimal standardDeviation = BigDecimal.valueOf(Math.sqrt(varience.doubleValue())).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal offSet = standardDeviation.multiply(standardDeviationMultiplier).setScale(SCALE, RoundingMode.HALF_UP);
        return new BollingerBands(middle.value().subtract(offSet), middle.value().add(offSet), middle.value());
    }

    public static TechnicalIndicator atr(List<PricePoint> prices, int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("ATR period must be positive");
        }
        validatePricePeriod(prices, period+1);
        List<PricePoint> orderedPrice = orderedPrices(prices);
        BigDecimal trueRangeSum = BigDecimal.ZERO;

        for (int index = orderedPrice.size()-period; index < orderedPrice.size(); index++) {
            PricePoint current = orderedPrice.get(index);
            PricePoint previous = orderedPrice.get(index-1);
            BigDecimal highLow = current.high().subtract(current.low());
            BigDecimal highClose = current.high().subtract(previous.close()).abs();
            BigDecimal lowClose = current.low().subtract(previous.close()).abs();
            trueRangeSum = trueRangeSum.add(highLow.max(highClose).max(lowClose));
        }

        BigDecimal value = trueRangeSum.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
        return indicator("ATR", value, Signal.NEUTRAL);

    }

    public static TechnicalIndicator volumeTrends(List<PricePoint> prices, int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("Volume trend period must be positive");
        }
        validatePricePeriod(prices, period * 2);

        List<PricePoint> orderedPrices = orderedPrices(prices);
        BigDecimal recentAverage = averageVolume(orderedPrices.subList(orderedPrices.size()-period, orderedPrices.size()));
        BigDecimal previousAverage = averageVolume(orderedPrices.subList(orderedPrices.size()-(period*2), orderedPrices.size()-period));
        if (previousAverage.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Volume trend is undefined when the previous average volume is zero");
        }
        BigDecimal change = recentAverage.subtract(previousAverage).divide(previousAverage, SCALE, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        Signal signal = change.compareTo(BigDecimal.TEN) > 0 ? Signal.BULLISH : change.compareTo(BigDecimal.TEN.negate()) < 0 ? Signal.BEARISH : Signal.NEUTRAL;
        return indicator("VOLUME_TREND", change.setScale(SCALE, RoundingMode.HALF_UP),signal);
    }

    public static BigDecimal fiftyTwoWeekHigh(List<PricePoint> prices) {
        return prices.stream().map(PricePoint::high).max(BigDecimal::compareTo).orElseThrow();
    }

   public static BigDecimal fiftyTwoWeekLow(List<PricePoint> prices) {
        return prices.stream().map(PricePoint::low).min(BigDecimal::compareTo).orElseThrow();
    }

    private static BigDecimal averageVolume(List<PricePoint> prices) {
        if (prices.isEmpty() || prices.stream().anyMatch(price -> price.volume() < 0)) {
            throw new IllegalArgumentException("Volume observations must be non-empty and non-negative");
        }
        BigDecimal totalVolume = prices.stream().map(price -> BigDecimal.valueOf(price.volume())).reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalVolume.divide(BigDecimal.valueOf(prices.size()), SCALE, RoundingMode.HALF_UP);
    }

    private static List<BigDecimal> emaSeries(List<BigDecimal> values, int period) {
        validateNumericPeriod(values, period);
        BigDecimal multiplier = BigDecimal.valueOf(2)
                .divide(BigDecimal.valueOf(period + 1L), WORKING_SCALE, RoundingMode.HALF_UP);
        BigDecimal current = values.subList(0, period).stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
        List<BigDecimal> result = new ArrayList<>(Collections.nCopies(values.size(), null));
        result.set(period - 1, current);
        for (int index = period; index < values.size(); index++) {
            current = values.get(index).subtract(current).multiply(multiplier).add(current)
                    .setScale(SCALE, RoundingMode.HALF_UP);
            result.set(index, current);
        }
        return result;
    }

    private static void validateNumericPeriod(List<BigDecimal> values, int period) {
        if (period <= 0 || values == null || values.size() < period
                || values.stream().anyMatch(value -> value == null)) {
            throw new IllegalArgumentException(
                    "Not enough numeric values for period=" + period + ", available=" + (values == null ? 0 : values.size())
            );
        }
    }

    private static List<PricePoint> orderedPrices(List<PricePoint> prices) {
        if (prices == null || prices.stream().anyMatch(price -> price == null || price.date() == null
                || price.high() == null || price.low() == null || price.close() == null)) {
            throw new IllegalArgumentException("Price points must contain dates and numeric OHLC values");
        }
        Set<LocalDate> dates = new HashSet<>();
        for (PricePoint price : prices) {
            if (!dates.add(price.date())) {
                throw new IllegalArgumentException("Price points contain duplicate dates");
            }
            if (price.high().compareTo(price.low()) < 0 || price.volume() < 0) {
                throw new IllegalArgumentException("Price point OHLC/volume values are invalid");
            }
        }
        return prices.stream().sorted(Comparator.comparing(PricePoint::date)).toList();
    }

    private static void validatePricePeriod(List<PricePoint> values, int period) {
        if (period <= 0 || values == null || values.isEmpty() || values.size() < period) {
            throw new IllegalArgumentException(
                    "Not enough price points for period=" + period + ", available=" + (values == null ? 0 : values.size())
            );
        }
    }

    private static TechnicalIndicator indicator(String name, BigDecimal value, Signal signal) {
        return new TechnicalIndicator(name, value, signal, LocalDateTime.now(), "market-data");
    }

    private static void validatePeriod(List<BigDecimal> values, int period) {
        if (period <= 0 || values == null || values.isEmpty() || values.size() < period
                || values.stream().anyMatch(value -> value == null || value.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException(
                    "Not enough values for period=" + period + ", available=" + (values == null ? 0 : values.size())
            );
        }
    }
}
