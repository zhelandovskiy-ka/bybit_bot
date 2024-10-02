package ru.ka_zhelandovskiy.bybit_bot.services;

import ru.ka_zhelandovskiy.bybit_bot.dto.SideProfitDto;
import ru.ka_zhelandovskiy.bybit_bot.models.StatisticsModel;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;

import java.util.List;
import java.util.Map;

public interface StatisticsService {
    /**
     * Добавить запись в таблицу statistics
     */
    StatisticsModel addRecord(Strategy strategy);

    /**
     * Получить процент проходимости по названию инструмента и стратегии
     */
    double getPercentByInstrumentAndStrategy(String instName, String strName);

    /**
     * Получить сумму профита по названию инструмента и стратегии
     */
    double getProfitByInstrumentAndStrategy(String instName, String strName);

    /**
     * Получить сумму профита по названию инструмента
     */
    double getProfitByInstrument(String instName);

    /**
     * Получить сумму профита количеством {@code limit} по названию стратегии
     */
    List<Double> getProfitSumByStrategy(String strategyName, int limit);

    /**
     * Получить сумму профита количеством {@code limit} по названию стратегии и инструмента
     */
    List<Double> getProfitSumByStrategyAndInstrument(String strategyName, String instrument, int limit);

    Map<Integer, Double> getBankDay24Sum(String strategyName);

    List<Double> getBankDaySum(String strategyName);

    Map<String, Double> getProfitSumByAllInstruments(String strategyName);

    Map<String, Double> getProfitSidesByStrategy(String strategyName);

    List<SideProfitDto> getProfitSidesByStrategyAndInstruments(String strategyName);
}
