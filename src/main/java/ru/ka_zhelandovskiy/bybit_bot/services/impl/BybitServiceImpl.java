package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.TriggerBy;
import com.bybit.api.client.domain.account.AccountType;
import com.bybit.api.client.domain.account.request.AccountDataRequest;
import com.bybit.api.client.domain.market.MarketInterval;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.restApi.BybitApiAccountRestClient;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.restApi.BybitApiTradeRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.configurations.IntervalsConfig;
import ru.ka_zhelandovskiy.bybit_bot.dto.Candlestick;
import ru.ka_zhelandovskiy.bybit_bot.dto.Kline;
import ru.ka_zhelandovskiy.bybit_bot.mapper.CandlestickMapper;
import ru.ka_zhelandovskiy.bybit_bot.services.BybitService;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;

import java.util.LinkedHashMap;
import java.util.List;

@Service
public class BybitServiceImpl implements BybitService {
    private final BybitApiClientFactory client;
    private final CandlestickMapper candlestickMapper;
    private final IntervalsConfig intervalsConfig;

    public BybitServiceImpl(CandlestickMapper candlestickMapper, IntervalsConfig intervalsConfig, ParameterService parameterService) {
        this.candlestickMapper = candlestickMapper;
        this.intervalsConfig = intervalsConfig;

        String apiKey = parameterService.getApiKey();
        String secretKey = parameterService.getSecretKey();

        this.client = BybitApiClientFactory.newInstance(apiKey, secretKey);
    }

    @Override
    public String placeOrder(String symbol, String qty, Side side) {
        BybitApiTradeRestClient tradeRestClient = client.newTradeRestClient();
        TradeOrderRequest tradeOrderRequest = TradeOrderRequest.builder()
                .symbol(symbol)
                .side(side)
                .qty(qty)
                .isLeverage(1)
                .category(CategoryType.LINEAR)
                .orderType(TradeOrderType.MARKET)
                .triggerBy(TriggerBy.MARK_PRICE)
                .slTriggerBy(TriggerBy.MARK_PRICE)
                .tpTriggerBy(TriggerBy.MARK_PRICE)
                .build();

        return tradeRestClient.createOrder(tradeOrderRequest).toString();
    }

    @Override
    public String closeOrder(String symbol, Side side) {
        BybitApiTradeRestClient tradeRestClient = client.newTradeRestClient();
        TradeOrderRequest tradeOrderRequest = TradeOrderRequest.builder()
                .symbol(symbol)
                .side(side)
                .qty("0")
                .isLeverage(1)
                .reduceOnly(true)
                .category(CategoryType.LINEAR)
                .orderType(TradeOrderType.MARKET)
                .triggerBy(TriggerBy.MARK_PRICE)
//                .slTriggerBy(TriggerBy.MARK_PRICE)
//                .tpTriggerBy(TriggerBy.MARK_PRICE)
                .build();

        return tradeRestClient.createOrder(tradeOrderRequest).toString();
    }

    @Override
    public String getWalletBalance(String coin) {
        BybitApiAccountRestClient bybitApiAccountRestClient = client.newAccountRestClient();
        return bybitApiAccountRestClient
                .getWalletBalance(AccountDataRequest.builder()
                        .accountType(AccountType.UNIFIED)
                        .coin(coin)
                        .build())
                .toString();
    }

    @Override
    public List<Candlestick> getCandleStickHistory(String symbol, int limit) {
        BybitApiMarketRestClient marketDataRestClient = client.newMarketDataRestClient();

        MarketDataRequest marketDataRequest = MarketDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(symbol)
                .marketInterval(intervalsConfig.getInterval())
                .limit(limit)
                .build();

        LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) marketDataRestClient.getMarketLinesData(marketDataRequest);
        ObjectMapper mapper = new ObjectMapper();
        Kline kline = mapper.convertValue(map, Kline.class);

        return candlestickMapper.mapListObjectToListCandlestick(kline.getResult().getList());
    }

    @Override
    public List<Candlestick> getCandleStickHistoryWithInterval(String symbol, int limit, MarketInterval interval) {
        BybitApiMarketRestClient marketDataRestClient = client.newMarketDataRestClient();

        MarketDataRequest marketDataRequest = MarketDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(symbol)
                .marketInterval(interval)
                .limit(limit)
                .build();

        LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) marketDataRestClient.getMarketLinesData(marketDataRequest);
        ObjectMapper mapper = new ObjectMapper();
        Kline kline = mapper.convertValue(map, Kline.class);

        return candlestickMapper.mapListObjectToListCandlestick(kline.getResult().getList());
    }

    @Override
    public double getCurrentPrice(String symbol) {
        return getCandleStickHistory(symbol, 1)
                .stream()
                .map(Candlestick::getPriceClose)
                .findFirst()
                .orElse(-1.0);
    }
}
