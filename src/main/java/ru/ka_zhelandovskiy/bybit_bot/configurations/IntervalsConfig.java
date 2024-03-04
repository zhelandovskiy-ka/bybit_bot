package ru.ka_zhelandovskiy.bybit_bot.configurations;

import com.bybit.api.client.domain.market.MarketInterval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.ka_zhelandovskiy.bybit_bot.models.enums.Parameter;
import ru.ka_zhelandovskiy.bybit_bot.repository.ParametersRepository;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;

@Repository
public class IntervalsConfig {
    private final ParameterService parameterService;

    public IntervalsConfig(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public MarketInterval getInterval() {
        int timeFrame = parameterService.getTimeFrame();

        return switch (timeFrame) {
            case 120 -> MarketInterval.TWO_HOURLY;
            case 5 -> MarketInterval.FIVE_MINUTES;
            default -> null;
        };
    }
}
