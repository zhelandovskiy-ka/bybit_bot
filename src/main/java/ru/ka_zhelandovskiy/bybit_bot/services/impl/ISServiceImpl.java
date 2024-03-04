package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import lombok.Getter;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.services.ISService;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;
import ru.ka_zhelandovskiy.bybit_bot.strategies.MaxChangeStrategy;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;
import ru.ka_zhelandovskiy.bybit_bot.util.StrategyName;

import java.util.ArrayList;
import java.util.List;

@Service
@Getter
public class ISServiceImpl implements ISService {
    private final InstrumentService instrumentService;
    private final StrategyService strategyService;
    private final List<Strategy> strategyList = new ArrayList<>();
    private final List<Strategy> finalStrategyList = new ArrayList<>();


    public ISServiceImpl(InstrumentService instrumentService, StrategyService strategyService) {
        this.instrumentService = instrumentService;
        this.strategyService = strategyService;
        buildStrategyList();

        System.out.println("STRATEGY LIST V1");
        strategyList.forEach(System.out::println);

        generateStrategyList();
        System.out.println("STRATEGY LIST V2");
        finalStrategyList.forEach(System.out::println);
    }

    public void buildStrategyList() {
        instrumentService.getSymbolList()
                .forEach(symbol ->
                        strategyList.addAll(strategyService.getStrategyList()
                                .stream()
                                .map(strategy -> {
                                    strategy.setInstrumentName(symbol);
                                    return new Strategy(strategy);
                                })
                                .toList()
                        ));
    }

    void generateStrategyList() {
        strategyList.forEach(strategy -> {

            switch (strategy.getType()) {
                case StrategyName.maxChange -> {
                    finalStrategyList.add(new MaxChangeStrategy(strategy));
                }
            }
        });
    }
}