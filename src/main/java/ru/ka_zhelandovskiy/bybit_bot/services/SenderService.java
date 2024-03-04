package ru.ka_zhelandovskiy.bybit_bot.services;

public interface SenderService {
    void send(String fileName, String channelId, String message);
}
