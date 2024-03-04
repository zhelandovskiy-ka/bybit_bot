package ru.ka_zhelandovskiy.bybit_bot.services;

import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.trade.Side;
import ru.ka_zhelandovskiy.bybit_bot.dto.Candlestick;

import java.util.List;

public interface BybitService {
    String placeOrder(String symbol, String qty, Side side);

    String closeOrder(String symbol, Side side);

    String getWalletBalance(String coin);

    List<Candlestick> getCandleStickHistory(String symbol, int limit);

    List<Candlestick> getCandleStickHistoryWithInterval(String symbol, int limit, MarketInterval interval);

    double getCurrentPrice(String symbol);
}
