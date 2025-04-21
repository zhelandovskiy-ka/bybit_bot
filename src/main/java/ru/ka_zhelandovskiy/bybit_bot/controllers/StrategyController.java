package ru.ka_zhelandovskiy.bybit_bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ka_zhelandovskiy.bybit_bot.services.ResultService;
import ru.ka_zhelandovskiy.bybit_bot.services.StatisticsService;

@RestController
@RequestMapping
@RequiredArgsConstructor
@CrossOrigin(origins = "${config.url:http://localhost:8081}")
public class StrategyController {

    private final StatisticsService statisticsService;
    private final ResultService resultsService;

    @PostMapping("/undo/{strategyName}")
    public void undoStrategyData(@PathVariable String strategyName) {

        statisticsService.deleteAllByStrategyName(strategyName);
        resultsService.resetAll(strategyName);
    }
}