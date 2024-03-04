package ru.ka_zhelandovskiy.bybit_bot.services;

import ru.ka_zhelandovskiy.bybit_bot.dto.SumType;
import ru.ka_zhelandovskiy.bybit_bot.models.ParametersModel;

import java.util.List;


public interface ParameterService {
    ParametersModel getParameter(String parameter);

    List<ParametersModel> getAllParameters();

    boolean isTestMode();

    double getFee();

    double getSum();

    double getSumByType(SumType sumType);

    double getRealSum();

    String getBotToken();

    String getBotName();

    int getTimeFrame();

    String getApiKey();

    String getSecretKey();

    void showCurrentParameters();
}
