package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderResult {

    private String orderId;
    private String orderLinkId;
}
