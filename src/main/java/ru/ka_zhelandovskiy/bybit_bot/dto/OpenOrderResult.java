package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OpenOrderResult {

    private String category;
    private List<Order> list = new ArrayList<>();
}
