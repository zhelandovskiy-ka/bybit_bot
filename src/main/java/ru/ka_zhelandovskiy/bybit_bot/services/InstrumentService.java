package ru.ka_zhelandovskiy.bybit_bot.services;

import ru.ka_zhelandovskiy.bybit_bot.dto.Instrument;
import ru.ka_zhelandovskiy.bybit_bot.dto.SumType;

import java.util.List;

public interface InstrumentService {
    List<Instrument> initInstrumentList();

    Instrument getInstrumentByName(String symbol);

    List<String> getSymbolList();

    List<Instrument> getInstrumentList();

    void refreshCandlesticks();

    String getQuantity(String symbol);

    double getSumWithLeverage(SumType sumType, String symbol);

    double getSumOfFee(String symbol);

    int getLeverageBySymbol(String symbol);

    double getCurrentPrice(String symbol);
}
