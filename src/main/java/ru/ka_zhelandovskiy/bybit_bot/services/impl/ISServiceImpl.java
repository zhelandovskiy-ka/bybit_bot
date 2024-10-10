package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.services.ISService;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyStorageService;
import ru.ka_zhelandovskiy.bybit_bot.strategies.*;
import ru.ka_zhelandovskiy.bybit_bot.util.StrategyName;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Getter
public class ISServiceImpl implements ISService {
    private final InstrumentService instrumentService;
    private final StrategyService strategyService;
    private final StrategyStorageService strategyStorageService;
    private final List<Strategy> strategyList = new ArrayList<>();
    private List<Strategy> finalStrategyList = new ArrayList<>();


    public ISServiceImpl(InstrumentService instrumentService, StrategyService strategyService, StrategyStorageService strategyStorageService) {
        this.instrumentService = instrumentService;
        this.strategyService = strategyService;
        this.strategyStorageService = strategyStorageService;

        if (!strategyStorageService.saveFileExist()) {
            buildStrategyList();

            log.info("SAVE FILE NOT FOUND");

            log.info("STRATEGY LIST V1");
            strategyList.forEach(System.out::println);

            generateStrategyList();
            log.info("STRATEGY LIST V2");
        } else {
            log.info("SAVE FILE FOUND, LOAD DATA");
            List<Strategy> loaded = strategyStorageService.load();
            setFinalStrategyList(loaded);
        }

        log.info("STRATEGY LIST:");
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
                case StrategyName.maxChange -> finalStrategyList.add(new MaxChangeStrategy(strategy));
                case StrategyName.maxChangeSimple -> finalStrategyList.add(new MaxChangeSimpleStrategy(strategy));
                case StrategyName.maxChangeNew -> finalStrategyList.add(new MaxChangeNewStrategy(strategy));
                case StrategyName.scalpStrategy -> finalStrategyList.add(new ScalpMinMaxVolStrategy(strategy));
                case StrategyName.smaStrategy -> finalStrategyList.add(new CrossSmaStrategy(strategy));
            }
        });
    }

    @Override
    public void setFinalStrategyList(List<Strategy> strategyList) {
        finalStrategyList = new ArrayList<>(strategyList);
    }
}