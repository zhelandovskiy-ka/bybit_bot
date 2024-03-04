package ru.ka_zhelandovskiy.bybit_bot.mapper;

import org.mapstruct.Mapper;
import ru.ka_zhelandovskiy.bybit_bot.dto.Instrument;
import ru.ka_zhelandovskiy.bybit_bot.models.InstrumentModel;

@Mapper(componentModel = "spring")
public interface InstrumentMapper {
    Instrument mapModelToDto(InstrumentModel instrumentModel);
}
