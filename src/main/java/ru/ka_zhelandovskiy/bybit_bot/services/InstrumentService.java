package ru.ka_zhelandovskiy.bybit_bot.services;

import ru.ka_zhelandovskiy.bybit_bot.dto.Instrument;
import ru.ka_zhelandovskiy.bybit_bot.enums.SumType;

import java.util.List;

public interface InstrumentService {
    List<Instrument> initInstrumentList();

    Instrument getInstrumentByName(String symbol);

    List<String> getSymbolList();

    List<Instrument> getInstrumentList();

    void refreshCandlesticks(int limit);

    String getQuantity(String symbol, SumType sumType);

    String getQuantity(String symbol, SumType sumType, int leverage);

    double getSumWithLeverage(SumType sumType, String symbol);

    double getSumOfFee(String symbol);

    int getLeverageBySymbol(String symbol);

    double getCurrentPrice(String symbol);
}
