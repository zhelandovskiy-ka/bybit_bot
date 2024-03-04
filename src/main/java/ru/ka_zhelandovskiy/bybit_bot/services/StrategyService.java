package ru.ka_zhelandovskiy.bybit_bot.services;

import ru.ka_zhelandovskiy.bybit_bot.models.StrategyModel;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;

import java.util.List;

public interface StrategyService {
    List<Strategy> initStrategyList();
    List<StrategyModel> getAllStrategiesByTimeFrame(int timeFrame);

    Strategy getStrategyByName(String name);

    void updateStrategy(Strategy strategy);

    List<Strategy> getStrategyList();

    void send(Strategy str);

    double getProfitPercent(Strategy strategy);

    double getProfitPercent(double priceOpen, double price);

    void calcProfitSum(Strategy s);

    void calcMaxProfitLosePercent(Strategy str);

    void resetSLTPPercent(Strategy str);

    void resetSide(Strategy str);
}
