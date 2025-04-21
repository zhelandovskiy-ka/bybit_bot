package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.Data;

import java.util.List;

@Data
public class KlineResult {
    private String symbol;
    private String category;
    private List<List<Object>> list;
}
