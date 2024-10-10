package ru.ka_zhelandovskiy.bybit_bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ka_zhelandovskiy.bybit_bot.services.ISService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyStorageService;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping
public class StorageController {

    private final StrategyStorageService strategyStorageService;
    private final ISService isService;

    @PostMapping("/save")
    public void save() {
        strategyStorageService.save(isService.getFinalStrategyList());
    }

    @PostMapping("/load")
    public List<Strategy> load() {
        return strategyStorageService.load();
    }

}

