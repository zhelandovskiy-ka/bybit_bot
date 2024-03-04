package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.dto.Instrument;
import ru.ka_zhelandovskiy.bybit_bot.dto.SumType;
import ru.ka_zhelandovskiy.bybit_bot.mapper.InstrumentMapper;
import ru.ka_zhelandovskiy.bybit_bot.repository.InstrumentRepository;
import ru.ka_zhelandovskiy.bybit_bot.services.BybitService;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InstrumentServiceImpl implements InstrumentService {
    private final InstrumentRepository instrumentRepository;
    private final InstrumentMapper instrumentMapper;
    private final ParameterService parameterService;
    private final BybitService bybitService;
    @Getter
    public List<Instrument> instrumentList;

    public InstrumentServiceImpl(InstrumentRepository instrumentRepository, InstrumentMapper instrumentMapper, ParameterService parameterService, BybitService bybitService) {
        this.instrumentRepository = instrumentRepository;
        this.instrumentMapper = instrumentMapper;
        this.parameterService = parameterService;
        this.bybitService = bybitService;
        instrumentList = initInstrumentList();
    }

    @Override
    public List<Instrument> initInstrumentList() {
        return instrumentRepository.findAll()
                .stream()
                .map(instrumentMapper::mapModelToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Instrument getInstrumentByName(String symbol) {
        return getInstrumentList()
                .stream()
                .filter(i -> i.getSymbol().equals(symbol))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<String> getSymbolList() {
        return getInstrumentList()
                .stream()
                .map(Instrument::getSymbol)
                .collect(Collectors.toList());
    }

    @Override
    public void refreshCandlesticks() {
        log.info("start refreshCandlesticks");

        instrumentList.forEach(i -> i.setCandlestickList(bybitService.getCandleStickHistory(i.getSymbol(), 60)));

        log.info("end refreshCandlesticks");
    }

    @Override
    public String getQuantity(String symbol) {
        Instrument instrument = getInstrumentByName(symbol);

        int quantityPrecision = instrument.getQp();
        double price = instrument.getCurrentPrice();
        double sum = getSumWithLeverage(SumType.real_sum, symbol) / price;

        return String.valueOf(Utilities.roundDouble(sum, quantityPrecision));
    }
    @Override
    public double getSumWithLeverage(SumType sumType, String symbol) {
        int leverage = getInstrumentByName(symbol).getLeverage();
        double sum = parameterService.getSumByType(sumType);

        return sum * leverage;
    }

    @Override
    public double getSumOfFee(String symbol) {
        return getSumWithLeverage(SumType.sum, symbol) * parameterService.getFee() * 2;
    }

    @Override
    public int getLeverageBySymbol(String symbol) {
        return getInstrumentByName(symbol).getLeverage();
    }

    @Override
    public double getCurrentPrice(String symbol) {
        return getInstrumentByName(symbol).getCandlestickList().get(0).getPriceClose();
    }
}
