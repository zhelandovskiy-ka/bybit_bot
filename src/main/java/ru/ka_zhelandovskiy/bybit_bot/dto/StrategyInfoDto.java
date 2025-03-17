package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.Data;

@Data
public class StrategyInfoDto {
    private String name;
    private double bank;
    private double tpPercent;
    private double slPercent;
    private int allPlus;
    private int allMinus;
    private double avgWin;
    private double avgLose;
}
