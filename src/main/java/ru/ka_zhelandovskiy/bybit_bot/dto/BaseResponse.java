package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse {
    private int retCode;
    private String retMsg;
    private RetExtInfo retExtInfo;
    private Long time;
}
