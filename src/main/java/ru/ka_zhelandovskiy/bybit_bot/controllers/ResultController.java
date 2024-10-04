package ru.ka_zhelandovskiy.bybit_bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.ka_zhelandovskiy.bybit_bot.models.ResultsModel;
import ru.ka_zhelandovskiy.bybit_bot.services.ResultService;

@RestController
@RequestMapping("/result")
@RequiredArgsConstructor
@CrossOrigin(origins = "${config.url:http://localhost:8081}")
public class ResultController {
    private final ResultService resultService;

    @PostMapping("/undo/{id}")
    public ResultsModel undoResultById(@PathVariable int id) {
        return resultService.undoResult(id);
    }
}