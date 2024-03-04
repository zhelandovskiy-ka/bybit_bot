package ru.ka_zhelandovskiy.bybit_bot.services;

import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;
import ru.ka_zhelandovskiy.bybit_bot.models.ResultsModel;

public interface ResultService {
    ResultsModel getResult(String name);

    void incrementsResult(String name, double sum);

    void ResetDay(String name);

    void sendDayStats(Strategy str);

    String getResultMessage(String name);

    double getBank(String name);

    double getMaxProfit(String name);

    double getMaxLose(String name);

    double getDayMoney(String name);

}
