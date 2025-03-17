package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyStorageService;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StrategyStorageServiceImpl implements StrategyStorageService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void save(List<Strategy> strategyList) {
        File file = new File("strategies.json");

        try {
            objectMapper.writerFor(new TypeReference<List<Strategy>>() {}).writeValue(file, strategyList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Strategy> load() {
        List<Strategy> strategyList;
        File file = new File("strategies.json");

        try {
            strategyList = objectMapper.readerForListOf(Strategy.class).readValue(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return strategyList;
    }

    @Override
    public boolean saveFileExist() {
        return new File("strategies.json").exists();
    }
}
