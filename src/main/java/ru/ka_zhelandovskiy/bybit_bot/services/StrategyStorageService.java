package ru.ka_zhelandovskiy.bybit_bot.services;

import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;

import java.util.List;

public interface StrategyStorageService {
    void save(List<Strategy> strategyList);

    List<Strategy> load();

    boolean saveFileExist();
}