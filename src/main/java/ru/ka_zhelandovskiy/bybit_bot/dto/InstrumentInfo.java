package ru.ka_zhelandovskiy.bybit_bot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InstrumentInfo {
    private String name;
    private String volume;
    private String price;

    public long getUsdtVolume() {
        double volumeD = Double.parseDouble(volume);
        double priceD = Double.parseDouble(price);
        Double usdtVolume = volumeD * priceD;

        return usdtVolume.longValue();
    }
}
