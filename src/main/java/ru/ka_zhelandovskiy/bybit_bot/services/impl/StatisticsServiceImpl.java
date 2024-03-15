package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;
import ru.ka_zhelandovskiy.bybit_bot.models.StatisticsModel;
import ru.ka_zhelandovskiy.bybit_bot.repository.StatisticsRepository;
import ru.ka_zhelandovskiy.bybit_bot.services.StatisticsService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    private final StatisticsRepository statisticsRepository;

    public StatisticsServiceImpl(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public void addRecord(Strategy s) {
        int result = s.getProfitSum() < 0 ? 0 : 1;

        StatisticsModel sm = new StatisticsModel();
        sm.setStrategy(s.getName());
        sm.setSide(s.getSide().getTransactionSide());
        sm.setInstrument(s.getInstrumentName());
        sm.setOpen(s.getPriceOpen());
        sm.setClose(s.getPriceClose());
        sm.setProfit(s.getProfitSum());
        sm.setResult(result);
        sm.setMaxProfit(s.getProfitMax());
        sm.setMaxLose(s.getLoseMax());
        sm.setDate("CURRENT_TIMESTAMP");

        statisticsRepository.save(sm);
    }

    @Override
    public double getPercentByInstrumentAndStrategy(String instName, String strName) {
        double sizeAll = statisticsRepository.findByInstrumentAndStrategy(instName, strName).size();
        double sizeAllPlus = statisticsRepository.findByInstrumentAndStrategyAndResult(instName, strName, 1).size();

        return Utilities.roundDouble(sizeAllPlus / sizeAll * 100);
    }

    @Override
    public double getProfitByInstrumentAndStrategy(String instName, String strName) {
        return Utilities.roundDouble(statisticsRepository.findByInstrumentAndStrategyAndProfitIsNotNull(instName, strName)
                .stream()
                .mapToDouble(StatisticsModel::getProfit)
                .sum(),2);
    }
}
