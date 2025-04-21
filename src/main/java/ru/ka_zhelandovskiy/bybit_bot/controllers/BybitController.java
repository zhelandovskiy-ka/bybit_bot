package ru.ka_zhelandovskiy.bybit_bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ka_zhelandovskiy.bybit_bot.dto.InstrumentInfo;
import ru.ka_zhelandovskiy.bybit_bot.services.BybitService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bybit")
public class BybitController {
    private final BybitService bybitService;

    @GetMapping("/get/balance")
    public String getBalance() {
        return bybitService.getWalletBalance("BTC");
    }

    @GetMapping("/instruments")
    public List<InstrumentInfo> getInstrumentsInfo() {
        return bybitService.getInstrumentsInfos();
    }
}
