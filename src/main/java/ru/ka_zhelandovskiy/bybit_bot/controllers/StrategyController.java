package ru.ka_zhelandovskiy.bybit_bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.ka_zhelandovskiy.bybit_bot.dto.ResultSumDto;
import ru.ka_zhelandovskiy.bybit_bot.dto.SideProfitDto;
import ru.ka_zhelandovskiy.bybit_bot.dto.StrategyInfoDto;
import ru.ka_zhelandovskiy.bybit_bot.models.ResultsModel;
import ru.ka_zhelandovskiy.bybit_bot.models.StrategyModel;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.ResultService;
import ru.ka_zhelandovskiy.bybit_bot.services.StatisticsService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "${config.url:http://localhost:8081}")
@RequiredArgsConstructor
@RestController
@RequestMapping
public class StrategyController {

    private final StrategyService strategyService;
    private final InstrumentService instrumentService;
    private final StatisticsService statisticsService;
    private final ResultService resultsService;

    @GetMapping("/strategies")
    @ResponseBody
    public List<String> getStrategies() {
        return strategyService.getStrategiesNameActive();
    }

    @GetMapping("/strategy/{strategyName}")
    @ResponseBody
    public StrategyModel getStrategyInfo(@PathVariable String strategyName) {
        return strategyService.getStrategyModelByName(strategyName);
    }

    @GetMapping("/instruments")
    @ResponseBody
    public List<String> getInstruments() {
        return instrumentService.getSymbolList();
    }

    @GetMapping("/info/{strategyName}")
    @ResponseBody
    public ResultsModel getResultInfo(@PathVariable String strategyName) {
        return resultsService.getResult(strategyName);
    }

    @GetMapping("/info/strategies")
    @ResponseBody
    public List<StrategyInfoDto> getStrategiesInfo() {
        return strategyService.getStrategiesInfo();
    }

    @GetMapping("/chart/all")
    @ResponseBody
    public List<ResultSumDto> getResultsSum() {
        return strategyService.getStrategiesBankSum();
    }

    @GetMapping("/chart/all/{strategyName}")
    @ResponseBody
    public List<Double> getStrategyDataDayAll(@PathVariable String strategyName) {
        return statisticsService.getProfitSumByStrategy(strategyName, 0);
    }

    @GetMapping("/chart/day-24/{strategyName}")
    @ResponseBody
    public Map<Integer, Double> getStrategyDataDay24(@PathVariable String strategyName) {
        return statisticsService.getBankDay24Sum(strategyName);
    }

    @GetMapping("/chart/day-all/{strategyName}")
    @ResponseBody
    public List<Double> getStrategyDataDay(@PathVariable String strategyName) {
        return statisticsService.getBankDaySum(strategyName);
    }

    @GetMapping("/chart/instruments/{strategyName}")
    @ResponseBody
    public Map<String, Double> getStrategyDataInstrumentAll(@PathVariable String strategyName) {
        return statisticsService.getProfitSumByAllInstruments(strategyName);
    }

    @GetMapping("/chart/side/all/{strategyName}")
    @ResponseBody
    public Map<String, Double> getProfitSidesByStrategy(@PathVariable String strategyName) {
        return statisticsService.getProfitSidesByStrategy(strategyName);
    }


    @GetMapping("/chart/side/{strategyName}/all-instrument")
    @ResponseBody
    public List<SideProfitDto> getProfitSidesByStrategyAndInstrument(@PathVariable String strategyName) {
        return statisticsService.getProfitSidesByStrategyAndInstruments(strategyName);
    }

    @GetMapping("/chart/{strategyName}")
    @ResponseBody
    public List<Double> getStrategyChartData(@PathVariable String strategyName) {
        return statisticsService.getProfitSumByStrategy(strategyName, 100);
    }

    @GetMapping("/chart/{strategyName}/{instrument}")
    @ResponseBody
    public List<Double> getInstrumentChartData(@PathVariable String strategyName, @PathVariable String instrument) {
        return statisticsService.getProfitSumByStrategyAndInstrument(strategyName, instrument, 100);
    }
}

