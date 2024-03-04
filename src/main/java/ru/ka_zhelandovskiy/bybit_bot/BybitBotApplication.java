package ru.ka_zhelandovskiy.bybit_bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.ScannerService;

@EnableCaching
@EnableScheduling
@SpringBootApplication
@EnableAutoConfiguration
public class BybitBotApplication {
    @Autowired
    ScannerService scannerService;
    @Autowired
    InstrumentService instrumentService;

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(BybitBotApplication.class);
        builder.headless(false);
        builder.run(args);
    }

    @Scheduled(fixedDelay = 5000L)
    public void checkPrice() {
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
}
