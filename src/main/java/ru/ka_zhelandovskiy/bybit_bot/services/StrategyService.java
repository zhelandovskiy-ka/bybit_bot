package ru.ka_zhelandovskiy.bybit_bot.services;

import ru.ka_zhelandovskiy.bybit_bot.dto.StrategyInfoDto;
import ru.ka_zhelandovskiy.bybit_bot.models.StrategyModel;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;

import java.util.List;

public interface StrategyService {

    List<Strategy> initStrategyList();

    List<StrategyModel> getActiveStrategiesByTimeFrame(int timeFrame);

    Strategy getStrategyByName(String name);

    List<Strategy> getStrategyList();

    List<String> getStrategiesNameActive();

    boolean isOpenedStrategy();

    double getProfitPercent(Strategy strategy);

    double getProfitPercent(Strategy strategy, double priceOpen);

    double getPriceChangePercent(double priceOpen, double price);

    void send(Strategy str);

    void calcProfitSum(Strategy s);

    void calcMaxProfitLosePercent(Strategy str);

    void resetSLTPPercent(Strategy str);

    void resetSide(Strategy str);

    void updateStrategy(Strategy strategy);

    List<StrategyInfoDto> getStrategiesInfo();
}