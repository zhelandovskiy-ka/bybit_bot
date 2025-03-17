package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.trade.Side;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
import ru.ka_zhelandovskiy.bybit_bot.dto.Candlestick;
import ru.ka_zhelandovskiy.bybit_bot.dto.SumType;
import ru.ka_zhelandovskiy.bybit_bot.models.SmaResultsModel;
import ru.ka_zhelandovskiy.bybit_bot.services.BybitService;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.SmaResultsService;
import ru.ka_zhelandovskiy.bybit_bot.services.TradeToolsService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeToolsServiceImpl implements TradeToolsService {
    private final SmaResultsService smaResultsService;
    private final BybitService bybitService;
    private final InstrumentService instrumentService;

    @Override
    public void calcSMA() {
        //вычисление линий SMA
        List<String> symbolList = List.of(
//                "DOTUSDT",
//                "ETHUSDT",
//                "BTCUSDT",
//                "MKRUSDT",
//                "BNBUSDT",
//                "XRPUSDT",
//                "EOSUSDT",
//                "LTCUSDT",
                "AXSUSDT", //доделать 5
                "SOLUSDT"  //доделать 5
        );

        List<Integer> timeframeList = List.of(
                5
//                ,
//                30
//                ,
//                60
//                ,
//                120
//                ,
//                240
//                ,
//                360
//                ,
//                720
        );

        timeframeList.forEach(
                timeFrame -> symbolList.forEach(
                        symbol -> {

                            Long s00 = System.nanoTime();
                            List<Candlestick> candlestickBigList = new ArrayList<>();

                            int count = 10;
                            int limit = 1000;
                            Long thousandCandle = 7200000L * limit;
                            Long start = Instant.now().toEpochMilli() - (thousandCandle * count);

                            for (int i = 0; i < count; i++) {
                                Long end = start + thousandCandle;
//                                System.out.println(STR."start: \{start} end: \{end}");
                                List<Candlestick> candlestickList = bybitService.getCandleStickHistoryWithIntervalAndPeriod(symbol, limit, MarketInterval.TWO_HOURLY, start, end);
                                Collections.reverse(candlestickList);
                                candlestickBigList.addAll(candlestickList);
                                start = end;
                            }

                            //получение списка цен закрытия
                            List<Double> pricesCloseList = candlestickBigList
                                    .stream()
                                    .map(Candlestick::getPriceClose)
                                    .toList();

                            //получение списка цен открытия
                            List<Double> pricesOpenList = candlestickBigList
                                    .stream()
                                    .map(Candlestick::getPriceOpen)
                                    .toList();

                            double sum = instrumentService.getSumWithLeverage(SumType.sum, symbol);
                            double fee = instrumentService.getSumOfFee(symbol);

                            List<Double> smaMinList;
                            List<Double> smaMaxList;

                            for (int min = 2; min <= 10; min++) {
//                                for (int max = 8; max < 16; max++) {
                                for (int max = min + 1; max <= 15; max++) {
                                    Long s0 = System.nanoTime();
                                    log.info("s0: " + (s0 - s00) / 10_000_000.0 + "s");

                                    double allProfit = 0;
                                    double allPosProfit = 0;
                                    double allNegProfit = 0;
                                    int posCount = 0;
                                    int negCount = 0;

                                    smaMinList = new ArrayList<>();
                                    //получение списка SMA по мин значению (minSma)
                                    for (int i = 0; i < pricesCloseList.size() - min + 1; i++) {
                                        Double sma = pricesCloseList.subList(i, i + min)
                                                .stream()
                                                .mapToDouble(Double::doubleValue)
                                                .average()
                                                .orElse(Double.NaN);

                                        smaMinList.add(sma);
                                    }

                                    smaMaxList = new ArrayList<>();
                                    //получение списка SMA по макс значению (maxSma)
                                    for (int i = 0; i < pricesCloseList.size() - max + 1; i++) {
                                        Double sma = pricesCloseList.subList(i, i + max)
                                                .stream()
                                                .mapToDouble(Double::doubleValue)
                                                .average()
                                                .orElse(Double.NaN);

                                        smaMaxList.add(sma);
                                    }

                                    log.info(STR."\{smaMinList.size()} \{smaMaxList.size()}");

                                    Long s1 = System.nanoTime();
                                    log.info(STR."s1: \{(s1 - s0) / 10_000_000.0}s");

                                    //моделирование открытия/закрытия операций
                                    boolean isLongOpen = false;
                                    boolean isShortOpen = false;
                                    boolean positionOpen = false;
                                    double positionPriceOpen = 0;

                                    smaMinList = smaMinList
                                            .stream()
                                            .skip(smaMinList.size() - smaMaxList.size())
                                            .toList();

                                    List<Double> pricesOpenListCut = pricesOpenList
                                            .stream()
                                            .skip(pricesOpenList.size() - smaMaxList.size())
                                            .toList();

                                    Long s2 = System.nanoTime();
                                    log.info(STR."s2: \{(s2 - s1) / 10_000_000.0}s");

                                    for (int i = 0; i < smaMaxList.size(); i++) {
                                        double priceOpen = pricesOpenListCut.get(i);
                                        double smaMin = smaMinList.get(i);
                                        double smaMax = smaMaxList.get(i);
                                        double profit = 0;
//                        System.out.println(STR."check \{smaMin} >< \{smaMax} PO: \{priceOpen}");

                                        if (smaMin > smaMax) {
                                            if (positionOpen) {
                                                if (isShortOpen) {
                                                    profit = calcProfit(positionPriceOpen, priceOpen, Side.SELL, sum, fee);
                                                    allProfit += profit;
                                                    if (profit < 0) {
                                                        allNegProfit += profit;
                                                        negCount++;
                                                    }
                                                    if (profit > 0) {
                                                        allPosProfit += profit;
                                                        posCount++;
                                                    }
                                                    positionPriceOpen = priceOpen;
                                                    isLongOpen = true;
                                                    isShortOpen = false;
                                                }
                                            }
                                            if (!positionOpen) {
                                                positionPriceOpen = priceOpen;
                                                positionOpen = true;
                                                isLongOpen = true;
                                            }
                                        }

                                        if (smaMin < smaMax) {
                                            if (positionOpen) {
                                                if (isLongOpen) {
                                                    profit = calcProfit(positionPriceOpen, priceOpen, Side.BUY, sum, fee);
                                                    allProfit += profit;
                                                    if (profit < 0) {
                                                        allNegProfit += profit;
                                                        negCount++;
                                                    }
                                                    if (profit > 0) {
                                                        allPosProfit += profit;
                                                        posCount++;
                                                    }
                                                    positionPriceOpen = priceOpen;
                                                    isShortOpen = true;
                                                    isLongOpen = false;
                                                }
                                            }
                                            if (!positionOpen) {
                                                positionPriceOpen = priceOpen;
                                                positionOpen = true;
                                                isShortOpen = true;
                                            }
                                        }
                                    }
                                    Long s3 = System.nanoTime();
                                    log.info(STR."s3: \{(s3 - s2) / 10_000_000.0}s");

                                    double posAvg = Utilities.roundDouble(allPosProfit / posCount);
                                    double negAvg = Utilities.roundDouble(allNegProfit / negCount);
                                    double avgProfit = Utilities.roundDouble(allProfit / (posCount + negCount));
                                    double patency = Utilities.roundDouble((double) posCount / (posCount + negCount) * 100);

                                    SmaResultsModel smaResultsModel = new SmaResultsModel();
                                    smaResultsModel.setInstrument(symbol);
                                    smaResultsModel.setSmaMin(min);
                                    smaResultsModel.setSmaMax(max);
                                    smaResultsModel.setProfit(Utilities.roundDouble(allProfit));
                                    smaResultsModel.setAvgProfit(avgProfit);
                                    smaResultsModel.setAvgPositiveProfit(posAvg);
                                    smaResultsModel.setAvgNegativeProfit(negAvg);
                                    smaResultsModel.setPositiveCount(posCount);
                                    smaResultsModel.setNegativeCount(negCount);
                                    smaResultsModel.setPatency(patency);
                                    smaResultsModel.setTimeFrame(timeFrame);

                                    smaResultsService.addRecord(smaResultsModel);

                                    if (!smaMinList.isEmpty())
                                        smaMaxList.clear();
                                    if (!smaMaxList.isEmpty())
                                        smaMinList.clear();

                                    Long s4 = System.nanoTime();
                                    log.info(STR."s4: \{(s4 - s3) / 10_000_000.0}s");

                                    log.info(STR."\{symbol} timeframe: \{timeFrame}, min: \{min}, max: \{max} profit: \{allProfit}");
                                    log.info(STR."\{symbol} posAvg: \{posAvg}, negAvg: \{negAvg} avgProfit: \{avgProfit} +/-: \{posCount}/\{negCount} \{patency}%");
                                    System.out.println("-------------------");
                                }
                            }
                        }
                )
        );
    }


    private double calcProfit(double priceOpen, double priceClose, Side side, double sum, double fee) {
        double profit = 0;

        if (side == com.bybit.api.client.domain.trade.Side.BUY)
            profit = (priceClose - priceOpen) * (sum / priceOpen);
        if (side == com.bybit.api.client.domain.trade.Side.SELL)
            profit = (priceOpen - priceClose) * (sum / priceOpen);

        return profit - fee;
    }
}
