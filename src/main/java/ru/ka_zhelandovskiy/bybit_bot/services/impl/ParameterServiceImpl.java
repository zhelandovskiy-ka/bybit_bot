package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.dto.SumType;
import ru.ka_zhelandovskiy.bybit_bot.models.ParametersModel;
import ru.ka_zhelandovskiy.bybit_bot.models.enums.Parameter;
import ru.ka_zhelandovskiy.bybit_bot.repository.ParametersRepository;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;

import java.util.List;

@Slf4j
@Service
public class ParameterServiceImpl implements ParameterService {
    @Autowired
    private ParametersRepository parametersRepository;

    @Override
//    @Cacheable("parametersCache")
    public ParametersModel getParameter(String parameter) {
        return parametersRepository.findByParameter(parameter);
    }

    @Override
//    @Cacheable("parametersCache")
    public List<ParametersModel> getAllParameters() {
        return parametersRepository.findAll();
    }

    @Override
    public boolean isTestMode() {
        return Boolean.parseBoolean(getParameter(Parameter.test_mode.name()).getValue());
    }

    @Override
    public double getFee() {
        return Double.parseDouble(getParameter(Parameter.fee.name()).getValue());
    }

    @Override
    public double getSum() {
        return Double.parseDouble(getParameter(Parameter.sum.name()).getValue());
    }

    @Override
    public double getRealSum() {
        return Double.parseDouble(getParameter(Parameter.real_sum.name()).getValue());
    }

    @Override
    public double getSumByType(SumType sumType) {
        return Double.parseDouble(getParameter(sumType.name()).getValue());
    }

    @Override
    public String getBotToken() {
        return getParameter(Parameter.bot_token.name()).getValue();
    }

    @Override
    public String getBotName() {
        return getParameter(Parameter.bot_name.name()).getValue();
    }

    @Override
    public int getTimeFrame() {
        return Integer.parseInt(getParameter(Parameter.time_frame.name()).getValue());
    }

    @Override
    public String getApiKey() {
        return getParameter(Parameter.api_key.name()).getValue();
    }

    @Override
    public String getSecretKey() {
        return getParameter(Parameter.secret_key.name()).getValue();
    }

    @Override
    public void showCurrentParameters() {
        log.info("========================================");

        parametersRepository.findAll().forEach(p -> log.info(p.getParameter() + ": " + p.getValue()));

        log.info("========================================");
    }
}
