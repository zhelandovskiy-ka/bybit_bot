package ru.ka_zhelandovskiy.bybit_bot.services;

import ru.ka_zhelandovskiy.bybit_bot.models.StatisticsModel;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;

public interface StatisticsService {
    StatisticsModel addRecord(Strategy strategy);
    double getPercentByInstrumentAndStrategy(String instName, String strName);
    double getProfitByInstrumentAndStrategy(String instName, String strName);
}
