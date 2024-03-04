package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.configurations.TelegramConfig;
import ru.ka_zhelandovskiy.bybit_bot.services.SenderService;

@Service
public class SenderServiceImpl implements SenderService {
    private final TelegramConfig telegramConfig;

    public SenderServiceImpl(TelegramConfig telegramConfig) {
        this.telegramConfig = telegramConfig;
    }

    @Override
    public void send(String fileName, String channelId, String message) {
        if (!fileName.isEmpty())
            telegramConfig.telegramBot().sendMsg(channelId, message);
//            telegramConfig.telegramBot().sendScreen(fileName, channelId, message);
        else
            telegramConfig.telegramBot().sendMsg(channelId, message);
    }
}
