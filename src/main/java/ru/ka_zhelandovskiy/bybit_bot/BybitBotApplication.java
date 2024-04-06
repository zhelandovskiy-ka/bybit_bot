package ru.ka_zhelandovskiy.bybit_bot;

import com.bybit.api.client.domain.market.MarketInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.ka_zhelandovskiy.bybit_bot.services.BybitService;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.ScannerService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

import java.util.List;

@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableAutoConfiguration
public class BybitBotApplication {
    @Autowired
    ScannerService scannerService;
    @Autowired
    InstrumentService instrumentService;
    @Autowired
    BybitService bybitService;
    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(BybitBotApplication.class);
        builder.headless(false);
        builder.run(args);
    }

    @Scheduled(fixedDelay = 5000L)
    public void checkPrice() {
//        debug();
        instrumentService.refreshCandlesticks();
        scannerService.start();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void sendResult() {
/*        instrumentServiceOld.getInstrumentList()
                .forEach(inst ->
                        inst.getStrategyList()
                                .forEach(str -> resultService.sendDayStats(str))
                );*/
    }

    private void debug() {
        //нахождение среднего размера свечи в %
/*        instrumentService.getSymbolList().forEach(symbol -> {
            double avgPriceChange = bybitService.getCandleStickHistoryWithInterval(symbol, 10, MarketInterval.FIVE_MINUTES)
                    .stream()
                    .mapToDouble(candlestick -> {
                        double change = candlestick.getPriceHigh() - candlestick.getPriceLow();
                        double percent = change / candlestick.getPriceOpen() * 100;
//                        System.out.println(percent);
                        return percent;
                    })
                    .average()
                    .orElse(0);
            System.out.println(STR."\{symbol}: \{Utilities.roundDouble(avgPriceChange, 2)}%");
        });*/

        //нахождение среднего размера свечи в %
        instrumentService.getSymbolList().forEach(symbol -> {
//            System.out.println("---------------------------------");
            List<Double> avgPriceChangeList = bybitService.getCandleStickHistoryWithInterval(symbol, 2000, MarketInterval.FIVE_MINUTES)
                    .stream()
                    .map(candlestick -> {
                        double change = candlestick.getPriceHigh() - candlestick.getPriceLow();
                        double percent = change / candlestick.getPriceOpen() * 100;
//                        System.out.println(percent);
                        return percent;
                    })
                    .toList();

            int maxSize = (int) (avgPriceChangeList.size() * 0.2);

            //составление списка из maxSize максимальных значений
            List<Double> sortedList = avgPriceChangeList
                    .stream()
                    .sorted((a, b) -> Double.compare(b, a))
                    .limit(maxSize)
                    .toList();

            //нахождение среднего из списка sortedList
            double avgMaxPerc = sortedList
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0);

            System.out.println(STR."\{Utilities.roundDouble(avgMaxPerc, 3)}");
        });

        System.out.println("=====================================");

/*        System.out.println(bybitService.placeOrder("XRPUSDT", "19", Side.BUY));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(bybitService.closeOrder("XRPUSDT", Side.SELL));*/
    }
}
