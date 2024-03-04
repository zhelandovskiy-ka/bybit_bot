package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AverageMove {
    private double lineMinValue = 0;
    private double lineMaxValue = 0;
    private boolean crossOnUp = false;
    private boolean crossOnDown = false;
    private String direction = "";
    private int durationMove = 0;
}
