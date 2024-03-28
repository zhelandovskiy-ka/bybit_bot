package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ka_zhelandovskiy.bybit_bot.configurations.TelegramConfig;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;
import ru.ka_zhelandovskiy.bybit_bot.models.ResultsModel;
import ru.ka_zhelandovskiy.bybit_bot.repository.ResultsRepository;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;
import ru.ka_zhelandovskiy.bybit_bot.services.ResultService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

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
        return resultsRepository.findByName(name);
    }

    @Override
    @Transactional
    public void incrementsResult(String name, double sum) {
        ResultsModel result = getResult(name);

        if (sum < 0) {
            result.setDayMinus(result.getDayMinus() + 1);
            result.setAllMinus(result.getAllMinus() + 1);
            result.setAllMinusMoney(result.getAllMinusMoney() + 1);
        }

        if (sum > 0) {
            result.setDayPlus(result.getDayPlus() + 1);
            result.setAllPlus(result.getAllPlus() + 1);
            result.setAllPlusMoney(result.getAllPlusMoney() + 1);
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

        log.info(STR."SAVE RESULT: \{result.toString()}");

        resultsRepository.save(result);
    }

    @Override
    @Transactional
    public void ResetDay(String name) {
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
}
