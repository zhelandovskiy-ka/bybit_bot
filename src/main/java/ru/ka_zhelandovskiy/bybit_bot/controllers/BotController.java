package ru.ka_zhelandovskiy.bybit_bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ka_zhelandovskiy.bybit_bot.services.ResultService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bot")
public class BotController {
    private final ResultService resultService;

    @GetMapping("/banks")
    public String getCurrentBalances() {
        resultService.getAllResult().forEach(
                result -> System.out.println(STR."\{result.getName()}: \{result.getBank()}"));
        return "ok";
    }
}
