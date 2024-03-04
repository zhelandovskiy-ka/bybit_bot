package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.Data;

@Data
public class Candlestick {
    private Long time;
    private double priceOpen;
    private double priceHigh;
    private double priceLow;
    private double priceClose;
    private double volume;
    private double turnover;

}
