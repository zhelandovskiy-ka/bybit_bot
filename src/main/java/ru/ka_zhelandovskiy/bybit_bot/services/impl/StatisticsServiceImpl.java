package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import com.bybit.api.client.domain.trade.Side;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.ka_zhelandovskiy.bybit_bot.dto.SideProfitDto;
import ru.ka_zhelandovskiy.bybit_bot.models.StatisticsModel;
import ru.ka_zhelandovskiy.bybit_bot.repository.StatisticsRepository;
import ru.ka_zhelandovskiy.bybit_bot.services.StatisticsService;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final StatisticsRepository statisticsRepository;

    @Override
    public StatisticsModel addRecord(Strategy s) {
        StatisticsModel sm = new StatisticsModel();

        sm.setStrategy(s.getName());
        sm.setSide(s.getSide().getTransactionSide());
        sm.setInstrument(s.getInstrumentName());
        sm.setOpen(s.getPriceOpen());

        StatisticsModel statisticsModel = statisticsRepository.save(sm);
        
        log.info(STR."ADD NEW RECORD TO STATISTICS: \{statisticsModel.toString()}");

        return statisticsModel;
    }

    @Override
    public StatisticsModel updateRecord(Strategy s) {
        if (statisticsRepository.findById(s.getNumber()).isPresent()) {
            int result = s.getProfitSum() < 0 ? 0 : 1;

            StatisticsModel sm = new StatisticsModel();

            sm.setNumber(s.getNumber());
            sm.setClose(s.getPriceClose());
            sm.setProfit(s.getProfitSumWoFee());
            sm.setResult(result);
            sm.setMaxProfit(s.getProfitMax());
            sm.setMaxLose(s.getLoseMax());

            StatisticsModel statisticsModel = statisticsRepository.save(sm);

            log.info(STR."UPDATE RECORD STATISTICS: \{statisticsModel.toString()}");

            return statisticsModel;
        } else {
            log.info(STR."RECORD \{s.getNumber()} NOT FOUND");
        }

        return null;
    }

    @Override
    public boolean deleteRecord(int id) {
        statisticsRepository.deleteById(id);
        return true;
    }

    @Override
    public double getPercentByInstrumentAndStrategy(String instName, String strategyName) {
        double sizeAll = statisticsRepository.findByInstrumentAndStrategy(instName, strategyName).size();
        double sizeAllPlus = statisticsRepository.findByInstrumentAndStrategyAndResult(instName, strategyName, 1).size();

        return Utilities.roundDouble(sizeAllPlus / sizeAll * 100);
    }

    @Override
    public double getProfitByInstrumentAndStrategy(String instName, String strategyName) {
        return Utilities.roundDouble(statisticsRepository.findByInstrumentAndStrategyAndProfitIsNotNull(instName, strategyName)
                .stream()
                .mapToDouble(StatisticsModel::getProfit)
                .sum());
    }

    @Override
    public double getProfitByInstrument(String instName) {
        return Utilities.roundDouble(statisticsRepository.findByInstrumentAndProfitIsNotNull(instName)
                .stream()
                .mapToDouble(StatisticsModel::getProfit)
                .sum());
    }

    @Override
    public List<Double> getProfitSumByStrategy(String strategyName, int limit) {
        List<Double> list = statisticsRepository.findByStrategy(strategyName).stream()
                .mapToDouble(StatisticsModel::getProfit)
                .boxed().toList();

        return getSumProfit(list, limit);
    }

    @Override
    public List<Double> getProfitSumByStrategyAndInstrument(String strategyName, String instrument, int limit) {
        List<Double> list = statisticsRepository.findByInstrumentAndStrategy(instrument, strategyName).stream()
                .mapToDouble(StatisticsModel::getProfit)
                .boxed().toList();

        return getSumProfit(list, limit);
    }

    @Override
    public Map<Integer, Double> getBankDay24Sum(String strategyName) {
        LocalDateTime dateEnd = LocalDateTime.now();
        LocalDateTime dateStart = dateEnd.minusDays(1);

        List<StatisticsModel> statisticsModelList = statisticsRepository.findByStrategy(strategyName);

        List<Double> list = statisticsModelList.stream()
                .mapToDouble(StatisticsModel::getProfit)
                .boxed().toList();

        List<Double> sumProfits = getSumProfit(list, 0);

        Map<Integer, Double> dayMap = new HashMap<>();

        for (int i = 0; i < statisticsModelList.size(); i++) {
            StatisticsModel statisticsModel = statisticsModelList.get(i);
            if (statisticsModel.getDateOpen().isAfter(dateStart) && statisticsModel.getDateOpen().isBefore(dateEnd)) {
                dayMap.put(statisticsModel.getDateOpen().getHour(), sumProfits.get(i));
            }
        }

        Double firstNotNull = getFirstNotNull(dayMap);

        for (int i = 0; i < 24; i++) {
            if (dayMap.get(i) == null) {
                if (dayMap.get(i - 1) == null)
                    dayMap.put(i, firstNotNull);
                else
                    dayMap.put(i, dayMap.get(i - 1));
            }
        }

        return dayMap;
    }

    @Override
    public List<Double> getBankDaySum(String strategyName) {
        LocalDateTime dateEnd = LocalDateTime.now();
        LocalDateTime dateStart = dateEnd.minusDays(1);

        List<StatisticsModel> statisticsModelList = statisticsRepository.findByStrategy(strategyName);

        List<StatisticsModel> statisticsModelDayList = new ArrayList<>();

        statisticsModelList.forEach(statisticsModel -> {
            if (statisticsModel.getDateOpen().isAfter(dateStart) && statisticsModel.getDateOpen().isBefore(dateEnd)) {
                statisticsModelDayList.add(statisticsModel);
            }
        });

        List<Double> list = statisticsModelList.stream()
                .mapToDouble(StatisticsModel::getProfit)
                .boxed().toList();

        return getSumProfit(list, statisticsModelDayList.size());
    }

    @Override
    public Map<String, Double> getProfitSumByAllInstruments(String strategyName) {
        List<StatisticsModel> statisticsModelList = statisticsRepository.findByStrategy(strategyName);

        Map<String, Double> instrumentsProfit = new HashMap<>();

        for (StatisticsModel statisticsModel : statisticsModelList) {
            instrumentsProfit.merge(statisticsModel.getInstrument(), statisticsModel.getProfit(), Double::sum);
        }

        return instrumentsProfit;
    }

    @Override
    public Map<String, Double> getProfitSidesByStrategy(String strategyName) {
        List<StatisticsModel> statisticsModelList = statisticsRepository.findByStrategy(strategyName);

        Map<String, Double> sidesProfit = new HashMap<>();

        for (StatisticsModel statisticsModel : statisticsModelList) {
            sidesProfit.merge(statisticsModel.getSide(), statisticsModel.getProfit(), Double::sum);
        }

        return sidesProfit;
    }

    @Override
    public List<SideProfitDto> getProfitSidesByStrategyAndInstruments(String strategyName) {
        List<StatisticsModel> statisticsModelList = statisticsRepository.findByStrategy(strategyName);

        Set<String> instrumentNames = statisticsModelList.stream()
                .map(StatisticsModel::getInstrument)
                .collect(Collectors.toSet());

        List<SideProfitDto> sideProfitDtos = new ArrayList<>();

        instrumentNames.forEach(instrument -> {
            Map<String, Double> sidesProfit = new HashMap<>();
            for (StatisticsModel statisticsModel : statisticsModelList) {
                if (statisticsModel.getInstrument().equals(instrument))
                    sidesProfit.merge(statisticsModel.getSide(), statisticsModel.getProfit(), Double::sum);
            }

            Double sumBuy = sidesProfit.get(Side.BUY.getTransactionSide());
            Double sumSell = sidesProfit.get(Side.SELL.getTransactionSide());

            sideProfitDtos.add(new SideProfitDto(
                    instrument,
                    sumBuy == null ? 0 : sumBuy,
                    sumSell == null ? 0 : sumSell
            ));
        });

        return sideProfitDtos;
    }

    @Override
    public StatisticsModel getByNumber(int id) {
        return statisticsRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "record mot found"));
    }

    public static Double getFirstNotNull(Map<Integer, Double> map) {
        for (int i = 0; i < 24; i++) {
            Double value = map.get(i);
            if (value != null)
                return value;
        }

        return null;
    }

    private List<Double> getSumProfit(List<Double> list, int limit) {
        double sum = 0;
        List<Double> sumList = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            sumList.add(sum += list.get(i));
        }

        if (sumList.size() > limit && !(limit == 0))
            limit = sumList.size() - limit;
        if (sumList.size() < limit)
            limit = 0;

        return sumList.stream()
                .skip(limit)
                .toList();
    }
}
