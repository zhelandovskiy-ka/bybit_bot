package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import com.bybit.api.client.domain.trade.Side;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.dto.SumType;
import ru.ka_zhelandovskiy.bybit_bot.models.StatisticsModel;
import ru.ka_zhelandovskiy.bybit_bot.services.*;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScannerServiceImpl implements ScannerService {
    private final ParameterService parameterService;
    private final BybitService bybitService;
    private final StatisticsService statisticsService;
    private final ResultService resultService;
    private final StrategyStorageService strategyStorageService;
    private final ISService isService;

    @Override
    @Scheduled(fixedDelay = 1000L)
    public void start() {
        System.out.println("****************************");
        parameterService.getAllParameters().forEach(System.out::println);
        System.out.println("****************************");
        isService.getFinalStrategyList().forEach(System.out::println);
        System.out.println("****************************");

        InstrumentService instrumentService = isService.getInstrumentService();
        StrategyService strategyService = isService.getStrategyService();

        instrumentService.refreshCandlesticks(30);

        if (!parameterService.isTestMode()) {
            isService.getFinalStrategyList()
                    .forEach(str -> {
                        String instrumentName = str.getInstrumentName();
                        log.info("---------------------------------------------------------------");

                        boolean checkToOpen = false;
                        boolean checkToClose = false;

                        if (!str.isOpen()) {
                            log.info(STR."  CHECK TO OPEN: \{str.getName()} \{instrumentName}");

                            checkToOpen = str.checkToOpen(isService);

                            log.info(STR."  RETURN \{checkToOpen} \{str.getSide()}");
                        }

                        if (str.isOpen()) {
                            log.info(STR."  CHECK TO CLOSE: \{str.getName()} \{instrumentName}");

                            checkToClose = str.checkToClose(isService);

                            log.info(STR."  RETURN \{checkToClose} \{str.getSide()}");
                        }

                        if (checkToOpen) {
                            str.setOpen(true);

                            checkForPlaceOrder(str, instrumentService);

                            log.info("---------------------------------------------------------------");

                            strategyService.send(str);

                            StatisticsModel sm = statisticsService.addRecord(str);

                            str.setNumber(sm.getNumber());
                            strategyService.updateStrategy(str);
                        }

                        if (checkToClose) {
                            str.setOpen(false);
                            str.setPriceClose(instrumentService.getCurrentPrice(str.getInstrumentName()));

                            checkForCloseOrder(str);

                            strategyService.calcMaxProfitLosePercent(str);
                            strategyService.calcProfitSum(str);
                            resultService.incrementsResult(str.getName(), str.getProfitSumWoFee());
                            statisticsService.updateRecord(str);
                            strategyService.send(str);
                            strategyService.resetSLTPPercent(str);
                            strategyService.resetSide(str);
                        }

                    });
            System.out.println("----------------------");
            strategyStorageService.save(isService.getFinalStrategyList());
            log.info("FILE SAVED");
        }
    }

    private void checkForPlaceOrder(Strategy str, InstrumentService instrumentService) {
        if (!parameterService.isTestMode() && str.isAllowOrder()) {
            Map<String, Integer> leverages = (Map<String, Integer>) str.getParameters().get("leverages");
            String quantity;
            int leverage = 0;
            if (leverages != null) {
                leverage = leverages.get(str.getInstrumentName());
                quantity = instrumentService.getQuantity(str.getInstrumentName(), SumType.real_sum, leverage);
            } else
                quantity = instrumentService.getQuantity(str.getInstrumentName(), SumType.real_sum);

            log.info(STR."TRY OPEN ORDER: \{str.getInstrumentName()}(x\{leverage}) \{quantity} \{str.getSide()} \{str.getName()}");
            log.info(bybitService.placeOrder(str.getInstrumentName(), quantity, str.getSide()));
        }
    }

    private void checkForCloseOrder(Strategy str) {
        if (!parameterService.isTestMode() && str.isAllowOrder()) {
            log.info(STR."TRY CLOSE ORDER: \{str.getInstrumentName()} \{str.getSide()} \{str.getName()}");
            Side side = str.getSide() == Side.BUY ? Side.SELL : Side.BUY;
            log.info(bybitService.closeOrder(str.getInstrumentName(), side));
        }
    }

}