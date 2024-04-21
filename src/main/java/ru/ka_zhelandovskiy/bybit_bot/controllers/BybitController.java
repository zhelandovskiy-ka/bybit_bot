package ru.ka_zhelandovskiy.bybit_bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ka_zhelandovskiy.bybit_bot.repository.ParametersRepository;
import ru.ka_zhelandovskiy.bybit_bot.services.BybitService;
import ru.ka_zhelandovskiy.bybit_bot.services.ISService;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;
import ru.ka_zhelandovskiy.bybit_bot.services.ResultService;

@RestController
@RequiredArgsConstructor
public class BybitController {
    private final BybitService bybitService;
    private final ParametersRepository parametersRepository;
    private final ParameterService parameterService;
    private final ISService isService;
    private final ResultService resultService;

    @GetMapping("/get/balance")
    public String getBalance() {
        return bybitService.getWalletBalance("BTC");
    }
}
