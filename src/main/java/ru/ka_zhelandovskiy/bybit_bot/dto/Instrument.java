package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;

import java.util.List;

@Data
@ToString
@NoArgsConstructor
public class Instrument {
    private String symbol;
    private int leverage;
    private int qp;
    private boolean ignore;
    private boolean reverse;
    private AverageMove averageMove;
//    private double maxChange;
    private List<Candlestick> candlestickList;
//    private List<Strategy> strategyList;

    public double getMinPrice() {
        return candlestickList.stream()
                .mapToDouble(Candlestick::getPriceClose)
                .min()
                .orElse(0);
    }

    public double getMaxPrice() {
        return candlestickList.stream()
                .mapToDouble(Candlestick::getPriceClose)
                .max()
                .orElse(0);
    }

    public double getCurrentPrice() {
        return candlestickList.get(0).getPriceClose();
    }

    public int getLeverage() {
        return leverage;
    }
}
