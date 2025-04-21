 package ru.ka_zhelandovskiy.bybit_bot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OrderBookResult {

    @JsonProperty(value = "s")
    private String symbol;

    @JsonProperty(value = "b")
    private List<List<Double>> bid;

    @JsonProperty(value = "a")
    private List<List<Double>> ask;

    private Long ts;
    private Long u;
    private Long seq;
    private Long cts;
}
