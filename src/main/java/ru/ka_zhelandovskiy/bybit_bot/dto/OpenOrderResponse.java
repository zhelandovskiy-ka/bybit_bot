package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OpenOrderResponse extends BaseResponse {

    private OpenOrderResult result;
}
