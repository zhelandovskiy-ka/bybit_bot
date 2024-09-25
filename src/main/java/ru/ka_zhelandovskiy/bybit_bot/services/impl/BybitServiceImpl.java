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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.configurations.IntervalsConfig;
import ru.ka_zhelandovskiy.bybit_bot.dto.*;
import ru.ka_zhelandovskiy.bybit_bot.mapper.CandlestickMapper;
import ru.ka_zhelandovskiy.bybit_bot.services.BybitService;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;

import java.util.*;

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

    private String parseJson(Object response) {
        String jsonResponse = new Gson().toJson(response);

        // Преобразуем JSON строку в JsonObject
        JsonObject jsonObject = new Gson().fromJson(jsonResponse, JsonObject.class);

        JsonObject result = jsonObject.getAsJsonObject("result");

        JsonArray list = result.getAsJsonArray("list");

        JsonObject firstCandlestick = list.get(0).getAsJsonObject();

        return firstCandlestick.get("lastPrice").getAsString();
    }

    @Override
    public List<Candlestick> getCandleStickHistory(String symbol, int limit, MarketInterval interval) {

        BybitApiMarketRestClient marketDataRestClient = client.newMarketDataRestClient();

        if (interval == null)
            interval = intervalsConfig.getInterval();

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
    public List<Candlestick> getCandleStickHistoryWithIntervalAndPeriod(String symbol, int limit, MarketInterval interval, Long start, Long end) {
        BybitApiMarketRestClient marketDataRestClient = client.newMarketDataRestClient();

        MarketDataRequest marketDataRequest = MarketDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(symbol)
                .marketInterval(interval)
                .limit(limit)
                .start(start)
                .end(end)
                .build();

        LinkedHashMap<String, String> map = (LinkedHashMap<String, String>) marketDataRestClient.getMarketLinesData(marketDataRequest);
        ObjectMapper mapper = new ObjectMapper();
        Kline kline = mapper.convertValue(map, Kline.class);

        return candlestickMapper.mapListObjectToListCandlestick(kline.getResult().getList());
    }

    @Override
    public double getCurrentPrice(String symbol) {
        return getCandleStickHistory(symbol, 1, null)
                .stream()
                .map(Candlestick::getPriceClose)
                .findFirst()
                .orElse(-1.0);
    }

    @Override
    public double getCurrentPrice(String symbol, MarketInterval interval) {
        return getCandleStickHistory(symbol, 1, interval)
                .stream()
                .map(Candlestick::getPriceClose)
                .findFirst()
                .orElse(-1.0);
    }

    @Override
    public double getVolume(String symbol, MarketInterval interval) {
        return getCandleStickHistory(symbol, 1, interval)
                .stream()
                .map(Candlestick::getVolume)
                .findFirst()
                .orElse(-1.0);
    }

    @Override
    public List<InstrumentInfo> getInstrumentsInfos() {
        BybitApiMarketRestClient marketDataRestClient = client.newMarketDataRestClient();
        MarketDataRequest marketDataRequest = MarketDataRequest.builder()
                .category(CategoryType.SPOT)
                .build();

        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) marketDataRestClient.getMarketTickers(marketDataRequest);
        LinkedHashMap<String, Object> map2 = (LinkedHashMap<String, Object>) map.get("result");
        ArrayList<Map<String, String>> resultList = (ArrayList<Map<String, String>>) map2.get("list");

        return resultList.stream()
                .map(entry ->
                        new InstrumentInfo(
                                entry.get("symbol"),
                                entry.get("volume24h"),
                                entry.get("lastPrice")
                        )
                ).toList();
    }
}
