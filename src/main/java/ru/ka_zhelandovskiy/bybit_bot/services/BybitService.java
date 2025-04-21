package ru.ka_zhelandovskiy.bybit_bot.services;

import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.trade.Side;
import ru.ka_zhelandovskiy.bybit_bot.dto.Candlestick;
import ru.ka_zhelandovskiy.bybit_bot.dto.InstrumentInfo;
import ru.ka_zhelandovskiy.bybit_bot.dto.Order;
import ru.ka_zhelandovskiy.bybit_bot.dto.OrderBookResponse;
import ru.ka_zhelandovskiy.bybit_bot.dto.OrderResponse;

import java.util.List;

public interface BybitService {

    String placeOrder(String symbol, String qty, Side side);

    OrderResponse placeLimitOrder(String symbol, String qty, Side side, String price);

    OrderResponse modifyLimitOrder(Order order, String price);

    Order getOpenOrderById(String orderId);

    List<Order> getOpenOrder();

    String getPriceFromOrderBook(String symbol, Side side);

    String closeOrder(String symbol, Side side);

    String getWalletBalance(String coin);

    OrderBookResponse getOrderBook(String symbol, int limit);

    List<Candlestick> getCandleStickHistory(String symbol, int limit, MarketInterval interval);

    List<Candlestick> getCandleStickHistoryWithInterval(String symbol, int limit, MarketInterval interval);

    List<Candlestick> getCandleStickHistoryWithIntervalAndPeriod(String symbol, int limit, MarketInterval interval, Long start, Long end);

    double getCurrentPrice(String symbol);

    double getCurrentPrice(String symbol, MarketInterval interval);

    double getVolume(String symbol, MarketInterval interval);

    List<InstrumentInfo> getInstrumentsInfos();
}
