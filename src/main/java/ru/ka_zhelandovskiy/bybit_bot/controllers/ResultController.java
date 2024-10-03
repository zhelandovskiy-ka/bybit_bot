package ru.ka_zhelandovskiy.bybit_bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ka_zhelandovskiy.bybit_bot.models.ResultsModel;
import ru.ka_zhelandovskiy.bybit_bot.services.ResultService;

@RestController
@RequestMapping("/result")
@RequiredArgsConstructor
public class ResultController {
    private final ResultService resultService;

    @PostMapping("/undo/{id}")
    public ResultsModel undoResultById(@PathVariable int id) {
        return resultService.undoResult(id);
    }
}
