package ru.ka_zhelandovskiy.bybit_bot.services;

import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;

import java.util.List;

public interface ISService {
    List<Strategy> getStrategyList();

    List<Strategy> getFinalStrategyList();

    InstrumentService getInstrumentService();

    StrategyService getStrategyService();

    void setFinalStrategyList(List<Strategy> strategyList);
}
