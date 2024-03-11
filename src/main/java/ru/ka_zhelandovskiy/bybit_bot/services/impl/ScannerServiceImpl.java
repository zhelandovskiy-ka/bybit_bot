package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import com.bybit.api.client.domain.trade.Side;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.dto.Instrument;
import ru.ka_zhelandovskiy.bybit_bot.services.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScannerServiceImpl implements ScannerService {
    private final ParameterService parameterService;
    private final BybitService bybitService;
    private final StatisticsService statisticsService;

    private final ISService isService;

    @Override
    public void start() {
        System.out.println("****************************");
        parameterService.getAllParameters().forEach(System.out::println);
        System.out.println("****************************");
        isService.getFinalStrategyList().forEach(System.out::println);
        System.out.println("****************************");

        InstrumentService instrumentService = isService.getInstrumentService();
        StrategyService strategyService = isService.getStrategyService();

        isService.getFinalStrategyList()
                .forEach(str -> {
                    String instrumentName = str.getInstrumentName();
                    log.info("---------------------------------------------------------------");
                    Instrument instrument = instrumentService.getInstrumentByName(instrumentName);

                    boolean checkToOpen = false;
                    boolean checkToClose = false;

                    if (!str.isOpen()) {
                        log.info(STR."  CHECK TO OPEN: \{str.getName()} \{instrumentName} \{str.getSide()}");

                        checkToOpen = str.checkToOpen(isService);

                        log.info(STR."  RETURN \{checkToOpen}");

                    }
                    if (str.isOpen()) {
                        log.info(STR."  CHECK TO CLOSE: \{str.getName()} \{instrumentName} \{str.getSide()}");

                        checkToClose = str.checkToClose(isService);

                        log.info(STR."  RETURN \{checkToClose}");
                    }

                    if (checkToOpen) {
                        str.setOpen(true);
                        str.setPriceOpen(instrument.getCurrentPrice());

//                        strategyService.resetMaxMinPrice(str);
//                        strategyService.resetMaxProfitLose(str);

                        if (!parameterService.isTestMode() && str.getName().equals("maxCh_120SlMax")) {
                            String quantity = instrumentService.getQuantity(str.getInstrumentName());
                            log.info(STR."TRY OPEN ORDER: \{str.getInstrumentName()} \{quantity} \{str.getSide()} \{str.getName()}");

                            log.info(bybitService.placeOrder(str.getInstrumentName(), quantity, str.getSide()));
                        }

                        log.info("---------------------------------------------------------------");

                        strategyService.send(str);
                    }

                    if (checkToClose) {
                        str.setOpen(false);
                        str.setPriceClose(instrumentService.getCurrentPrice(str.getInstrumentName()));
                        if (!parameterService.isTestMode() && str.getName().equals("maxCh_120SlMax")) {
                            log.info(STR."TRY CLOSE ORDER: \{str.getInstrumentName()} \{str.getSide()} \{str.getName()}");

                            Side side = str.getSide() == Side.BUY ? Side.SELL : Side.BUY;

                            log.info(bybitService.closeOrder(str.getInstrumentName(), side));
                        }

                        strategyService.calcMaxProfitLosePercent(str);
                        strategyService.calcProfitSum(str);
                        strategyService.send(str);
                        statisticsService.addRecord(str);
                        strategyService.resetSLTPPercent(str);
                        strategyService.resetSide(str);

                    }

                });
        System.out.println("----------------------");
    }

}
