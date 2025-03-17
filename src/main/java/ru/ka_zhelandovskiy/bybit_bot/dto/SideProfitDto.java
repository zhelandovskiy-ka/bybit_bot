package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SideProfitDto {
    private String instrumentName;
    private Double buy;
    private Double sell;
}
