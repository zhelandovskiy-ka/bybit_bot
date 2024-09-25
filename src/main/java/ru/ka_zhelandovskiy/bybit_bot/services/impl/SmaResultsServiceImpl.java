package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.models.SmaResultsModel;
import ru.ka_zhelandovskiy.bybit_bot.repository.SmaResultsRepository;
import ru.ka_zhelandovskiy.bybit_bot.services.SmaResultsService;

@Service
@RequiredArgsConstructor
public class SmaResultsServiceImpl implements SmaResultsService {
    private final SmaResultsRepository smaResultsRepository;

    @Override
    public SmaResultsModel addRecord(SmaResultsModel smaResultsModel) {
        return smaResultsRepository.save(smaResultsModel);
    }
}
