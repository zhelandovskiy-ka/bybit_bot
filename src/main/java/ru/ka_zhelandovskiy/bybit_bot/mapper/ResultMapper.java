package ru.ka_zhelandovskiy.bybit_bot.mapper;

import org.mapstruct.Mapper;
import ru.ka_zhelandovskiy.bybit_bot.dto.StrategyInfoDto;
import ru.ka_zhelandovskiy.bybit_bot.models.ResultsModel;

@Mapper(componentModel = "spring")
public interface ResultMapper {

    StrategyInfoDto toStrategyInfoDto(ResultsModel resultsModel);
}
