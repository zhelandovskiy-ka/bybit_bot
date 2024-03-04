
package ru.ka_zhelandovskiy.bybit_bot.strategies;

import com.bybit.api.client.domain.trade.Side;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

import java.util.Map;

@Data
@NoArgsConstructor
public class Strategy {
    private String name;
    private String instrumentName;
    private String channelId;
    private String type;
    private boolean open;
    private Side side;
    private Map<String, Object> parameters;
    private double priceOpen;
    private double priceClose;
//    private double currentPrice;
    private double profitPercent;
    private double profitSum;
    private double slPercent;
    private double tpPercent;
//    private double priceMax;
//    private double priceMin;
    private double profitMax;
    private double loseMax;
    private double allBetSum = 0;

    public Strategy(String name, String channelId) {
        this.name = name;
        this.channelId = channelId;
    }

    public Strategy(Strategy strategy) {
        this.name = strategy.getName();
        this.channelId = strategy.getChannelId();
        this.instrumentName = strategy.getInstrumentName();
        this.parameters = strategy.getParameters();
        this.type = strategy.getType();
        this.slPercent = strategy.getSlPercent();
        this.tpPercent = strategy.getTpPercent();
    }

    @Override
    public String toString() {
        return "Strategy{" +
                "name='" + name + '\'' +
                ", instrumentName='" + instrumentName + '\'' +
                ", channelId='" + channelId + '\'' +
                ", parameters='" + parameters + '\'' +
                ", getType()=" + getType()+
                ", getTpPercent()=" + getTpPercent()+
                ", getSlPercent()=" + getSlPercent()+
                '}';
    }

    public String getMessageForSend(String result, double sumWithLeverage, double percent, double sum, double percentOfSum) {
        String isOpenClose = isOpen() ? "#open" : "#close";

        return "#" + getName() + " #" + getSide() + " " + isOpenClose
                + "\n\n" + "#" + getInstrumentName() + " PO: " + getPriceOpen() + " " + result
                + "\n\n" + "Ставка: " + sumWithLeverage
                + "\n\n" + getInstrumentName() + ": " + percent + "% | " + sum + "$ | " + percentOfSum + "%";
    }

    public boolean checkToOpen(InstrumentService is, StrategyService ss) {
        return false;
    };

    public boolean checkToClose(InstrumentService is, StrategyService ss) {
        return false;
    }

    public double getLoseMax() {
        return Utilities.roundDouble(loseMax);
    }

    public double getProfitMax() {
        return Utilities.roundDouble(profitMax);
    }

    public double getPriceOpen() {
        return Utilities.roundDouble(priceOpen);
    }

    public double getPriceClose() {
        return Utilities.roundDouble(priceClose);
    }
}
