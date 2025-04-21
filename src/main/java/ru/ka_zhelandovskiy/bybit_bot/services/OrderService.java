package ru.ka_zhelandovskiy.bybit_bot.services;

public interface OrderService {

    void updateOrdersPrice();

    void addOrderIdToList(String id);
}
