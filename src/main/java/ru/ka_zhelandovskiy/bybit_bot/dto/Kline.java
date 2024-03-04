package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.Data;

@Data
public class Kline {
    private int retCode;
    private String retMsg;
    private Result result;
    private RetExtInfo retExtInfo;
    private Long time;
}
