package ru.ka_zhelandovskiy.bybit_bot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StrategyType {
    MAX_CHANGE("maxChange"),
    MAX_CHANGE_SIMPLE("maxChangeSimple"),
    MAX_CHANGE_NEW("maxChangeNew"),
    MAX_CHANGE_TPSL("maxChangeTPSL"),
    SCALP_STRATEGY("scalpStrategy"),
    SMA_STRATEGY("smaStrategy");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}