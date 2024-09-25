package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.configurations.TelegramConfig;
import ru.ka_zhelandovskiy.bybit_bot.dto.ResultSumDto;
import ru.ka_zhelandovskiy.bybit_bot.mapper.ResultMapper;
import ru.ka_zhelandovskiy.bybit_bot.services.ISService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;
import ru.ka_zhelandovskiy.bybit_bot.models.ResultsModel;
import ru.ka_zhelandovskiy.bybit_bot.repository.ResultsRepository;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;
import ru.ka_zhelandovskiy.bybit_bot.services.ResultService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ResultServiceImpl implements ResultService {
    private final ResultsRepository resultsRepository;
    private final ParameterService parameterService;
    private final TelegramConfig telegramConfig;

    public ResultServiceImpl(ResultsRepository resultsRepository, ParameterService parameterService, TelegramConfig telegramConfig) {
        this.resultsRepository = resultsRepository;
        this.parameterService = parameterService;
        this.telegramConfig = telegramConfig;

        System.out.println("RESULTS: ");
        resultsRepository.findAll().forEach(System.out::println);
    }

    @Override
    public ResultsModel getResult(String name) {
        ResultsModel result = resultsRepository.findByName(name);

        if (result == null) {
            ResultsModel resultsModel = new ResultsModel();
            resultsModel.setName(name);

            return resultsRepository.save(resultsModel);
        }

        return result;
    }

    @Override
    public ResultsModel getOrInsert(String name) {
        ResultsModel resultsModel = getResult(name);
        if (resultsModel == null) {
            resultsModel = new ResultsModel();
            resultsModel.setName(name);
            return resultsRepository.save(resultsModel);
        }
        return resultsModel;
    }

    @Override
    public ResultsModel save(ResultsModel resultsModel) {
        return resultsRepository.save(resultsModel);
    }

    @Override
    public void incrementsResult(String name, double sum) {
        ResultsModel result = getResult(name);

        if (sum < 0) {
            result.setDayMinus(result.getDayMinus() + 1);
            result.setAllMinus(result.getAllMinus() + 1);
            result.setAllMinusMoney(result.getAllMinusMoney() + sum);
            result.setAvgLose(result.getAllMinusMoney() / result.getAllMinus());
        }

        if (sum > 0) {
            result.setDayPlus(result.getDayPlus() + 1);
            result.setAllPlus(result.getAllPlus() + 1);
            result.setAllPlusMoney(result.getAllPlusMoney() + sum);
            result.setAvgWin(result.getAllPlusMoney() / result.getAllPlus());
        }

        if (sum != 0) {
            result.setBank(result.getBank() + sum);
            result.setDayMoney(result.getDayMoney() + sum);
        }

        double profit = result.getBank() + sum - result.getStartBank();

        if (profit > 0)
            result.setMaxProfit(Math.max(result.getMaxProfit(), profit));
        if (profit < 0)
            result.setMaxLose(Math.min(result.getMaxLose(), profit));

        ResultsModel savedResult = resultsRepository.save(result);

        log.info(STR."SAVE RESULT: \{savedResult.toString()}");
    }

    @Override
    public void resetDay(String name) {
        ResultsModel result = getResult(name);
        result.setDayMinus(0);
        result.setDayPlus(0);
        result.setDayMoney(0);
        result.setMaxProfit(0);
        result.setMaxLose(0);
        result.setStartBank(0);

        resultsRepository.save(result);
    }

    @Override
    public void sendDayStats(Strategy str) {
        telegramConfig.telegramBot().sendMsg(str.getChannelId(), getResultMessage(str.getName()));
    }

    @Override
    public String getResultMessage(String name) {
        ResultsModel r = getResult(name);

        double dayPercent = Utilities.roundDouble((double) r.getDayPlus() / (r.getDayPlus() + r.getDayMinus()) * 100);
        double allPercent = Utilities.roundDouble((double) r.getAllPlus() / (r.getAllPlus() + r.getAllMinus()) * 100);
        double realDayProfit = r.getDayMoney() / (parameterService.getSum() / parameterService.getRealSum());

        return "#итоги #" + name
                + "\n\n" + r.getDayPlus() + " (" + r.getAllPlus() + ") ✅  " + r.getDayMinus() + " (" + r.getAllMinus() + ") ❌ " + dayPercent + "% (" + allPercent + "%)"
                + "\n\nПрибыль: " + r.getDayMoney() + " (" + realDayProfit + ")"
                + "\n\nСредний + с ордера: " + r.getAvgWin()
                + "\n\nСредний - с ордера: " + r.getAvgLose()
                + "\n\nМакс + за день: " + r.getMaxProfit()
                + "\n\nМакс - за день: " + r.getMaxLose()
                + "\n\nБанк: " + r.getBank() + "$";
    }

    @Override
    public double getBank(String name) {
        return Utilities.roundDouble(resultsRepository.findByName(name).getBank());
    }

    @Override
    public double getMaxProfit(String name) {
        return Utilities.roundDouble(resultsRepository.findByName(name).getMaxProfit());
    }

    @Override
    public double getMaxLose(String name) {
        return Utilities.roundDouble(resultsRepository.findByName(name).getMaxLose());
    }

    @Override
    public double getDayMoney(String name) {
        return Utilities.roundDouble(resultsRepository.findByName(name).getDayMoney());
    }

    @Override
    public List<ResultsModel> getAllResult() {
        return resultsRepository.findAll();
    }
}
