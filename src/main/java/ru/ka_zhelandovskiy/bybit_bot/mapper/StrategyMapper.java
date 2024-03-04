package ru.ka_zhelandovskiy.bybit_bot.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import ru.ka_zhelandovskiy.bybit_bot.models.StrategyModel;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface StrategyMapper {
    Strategy mapModelToStrategy(StrategyModel strategyModel);

    default Map<String, Object> mapStringToMap(String parameters) {
        Map<String, Object> map;
        try {
            map = new ObjectMapper().readValue(parameters, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return map;
    }
}
