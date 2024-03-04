package ru.ka_zhelandovskiy.bybit_bot.configurations;

import org.springframework.context.annotation.Configuration;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;
import ru.ka_zhelandovskiy.bybit_bot.telegram.TelegramBot;

@Configuration
public class TelegramConfig {
    private final ParameterService parameterService;

    public TelegramConfig(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public TelegramBot telegramBot() {
        return new TelegramBot(parameterService.getBotToken());
    }
}
