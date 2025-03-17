package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString
@NoArgsConstructor
public class Instrument {
    private String symbol;
    private int leverage;
    private int qp;
    private boolean ignore;
    private boolean reverse;
    private double maxChange;
    private List<Candlestick> candlestickList;
    private Map<Integer, Candlestick> candlesticksList;

    public double getMinPriceClose() {
        return candlestickList.stream()
                .mapToDouble(Candlestick::getPriceClose)
                .min()
                .orElse(0);
    }

    public double getMaxPriceClose() {
        return candlestickList.stream()
                .mapToDouble(Candlestick::getPriceClose)
                .max()
                .orElse(0);
    }

    public double getMaxPriceClose(double shiftCoefficient) {
        return candlestickList.stream()
                .mapToDouble(Candlestick::getPriceClose)
                .max()
                .orElse(0) * shiftCoefficient;
    }

    public double getMinPriceClose(double shiftCoefficient) {
        return candlestickList.stream()
                .mapToDouble(Candlestick::getPriceClose)
                .min()
                .orElse(0) * shiftCoefficient;
    }

    public double getMaxPriceHigh(double shiftCoefficient) {
        return candlestickList.stream()
                .mapToDouble(Candlestick::getPriceHigh)
                .max()
                .orElse(0) * shiftCoefficient;
    }

    public double getMinPriceLow(double shiftCoefficient) {
        return candlestickList.stream()
                .mapToDouble(Candlestick::getPriceLow)
                .min()
                .orElse(0) * shiftCoefficient;
    }

    public double getCurrentVolume() {
        return candlestickList.stream()
                .mapToDouble(Candlestick::getVolume)
                .findFirst()
                .orElse(-1);
    }

    public double getPrevVolume() {
        return candlestickList.stream()
                .skip(1)
                .mapToDouble(Candlestick::getVolume)
                .findFirst()
                .orElse(-1);
    }

    public double getAvgVolume() {
        return candlestickList.stream()
                .mapToDouble(Candlestick::getVolume)
                .average()
                .orElse(-1);
    }

    public Candlestick getCurrentCandlestick() {
        return candlestickList.stream()
                .findFirst()
                .orElse(null);
    }

    public Candlestick getPrevCandlestick() {
        return candlestickList.stream()
                .skip(1)
                .findFirst()
                .orElse(null);
    }

    public double getCurrentPrice() {
        return candlestickList.getFirst().getPriceClose();
    }

    public double getPrevPriceClose() {
        return candlestickList.stream()
                .skip(1)
                .mapToDouble(Candlestick::getPriceClose)
                .findFirst()
                .orElse(-1);
    }

    public Double getSMA(int size) {
        return candlestickList.stream()
                .limit(size)
                .mapToDouble(Candlestick::getPriceClose)
                .average()
                .orElse(Double.NaN);
    }

    public Double getPrevSMA(int size) {
        return getSMAWithShift(size, 1);
    }

    public Double getSMAWithShift(int size, int shift) {
        return candlestickList.stream()
                .skip(shift)
                .limit(size)
                .mapToDouble(Candlestick::getPriceClose)
                .average()
                .orElse(Double.NaN);
    }

    public Double getSMAShift(int size, int shift) {
        return candlestickList.stream()
                .limit(size)
                .mapToDouble(Candlestick::getPriceClose)
                .average()
                .orElse(Double.NaN);
    }
}
