package ru.ka_zhelandovskiy.bybit_bot.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ka_zhelandovskiy.bybit_bot.dto.Kline;
import ru.ka_zhelandovskiy.bybit_bot.repository.ParametersRepository;
import ru.ka_zhelandovskiy.bybit_bot.services.BybitService;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;

@RestController
public class BybitController {
    private final BybitService bybitService;
    private final ParametersRepository parametersRepository;
    private final ParameterService parameterService;

    public BybitController(BybitService bybitService, ParametersRepository parametersRepository, ParameterService parameterService) {
        this.bybitService = bybitService;
        this.parametersRepository = parametersRepository;
        this.parameterService = parameterService;
    }

    @GetMapping("/get/balance")
    public String getBalance() {
        return bybitService.getWalletBalance("BTC");
    }

    @GetMapping("/get/cl")
    public Kline getCandlestickHistory() {
        return null;
    }
}
