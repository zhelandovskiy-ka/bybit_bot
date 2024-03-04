package ru.ka_zhelandovskiy.bybit_bot.mapper;

import org.mapstruct.Mapper;
import ru.ka_zhelandovskiy.bybit_bot.dto.Candlestick;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CandlestickMapper {

    default List<Candlestick> mapListObjectToListCandlestick(List<List<Object>> listObject) {
        List<Candlestick> candlestickList = new ArrayList<>();

        listObject.forEach(list -> {
            Candlestick candlestick = new Candlestick();

            candlestick.setTime(Long.parseLong((String) list.get(0)));
            candlestick.setPriceOpen(Double.parseDouble((String) list.get(1)));
            candlestick.setPriceHigh(Double.parseDouble((String) list.get(2)));
            candlestick.setPriceLow(Double.parseDouble((String) list.get(3)));
            candlestick.setPriceClose(Double.parseDouble((String) list.get(4)));
            candlestick.setVolume(Double.parseDouble((String) list.get(5)));
            candlestick.setTurnover(Double.parseDouble((String) list.get(6)));

            candlestickList.add(candlestick);
        });

        return candlestickList;
    }
}
