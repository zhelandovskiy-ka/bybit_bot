package ru.ka_zhelandovskiy.bybit_bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ka_zhelandovskiy.bybit_bot.services.BybitService;

@RestController
@RequiredArgsConstructor
public class BybitController {
    private final BybitService bybitService;

    @GetMapping("/get/balance")
    public String getBalance() {
        return bybitService.getWalletBalance("BTC");
    }
}
