package ru.ka_zhelandovskiy.bybit_bot.configurations;

import com.bybit.api.client.domain.market.MarketInterval;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;

@Slf4j
@Configuration
public class IntervalsConfig {
    private final ParameterService parameterService;
    @Value("${bot.config.timeframe:0}")
    private int timeFrame;

    public IntervalsConfig(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public MarketInterval getInterval() {
        if (timeFrame == 0) {
            timeFrame = parameterService.getTimeFrame();
        }
        return getTimeframe(timeFrame);
    }

    private MarketInterval getTimeframe(int timeFrame) {
        return switch (timeFrame) {
            case 720 -> MarketInterval.TWELVE_HOURLY;
            case 360 -> MarketInterval.SIX_HOURLY;
            case 240 -> MarketInterval.FOUR_HOURLY;
            case 120 -> MarketInterval.TWO_HOURLY;
            case 60 -> MarketInterval.HOURLY;
            case 30 -> MarketInterval.HALF_HOURLY;
            case 5 -> MarketInterval.FIVE_MINUTES;
            case 1 -> MarketInterval.ONE_MINUTE;
            default -> null;
        };
    }
}
