
package ru.ka_zhelandovskiy.bybit_bot.strategies;

import com.bybit.api.client.domain.trade.Side;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ka_zhelandovskiy.bybit_bot.services.ISService;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

import java.util.Map;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "className"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MaxChangeStrategy.class, name = "maxChange"),
        @JsonSubTypes.Type(value = MaxChangeSimpleStrategy.class, name = "maxChangeSimple"),
        @JsonSubTypes.Type(value = MaxChangeNewStrategy.class, name = "maxChangeNew"),
        @JsonSubTypes.Type(value = ScalpMinMaxVolStrategy.class, name = "scalpStrategy"),
        @JsonSubTypes.Type(value = CrossSmaStrategy.class, name = "smaStrategy")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Strategy {
    private String name;
    private String instrumentName;
    private String channelId;
    private String type;
    private int timeFrame;
    private double priceOpen;
    private double previousPriceOpen;
    private double priceClose;
    private double profitPercent;
    private double profitSum;
    private double profitSumWoFee;
    private double slPercent;
    private double tpPercent;
    private double profitMax;
    private double loseMax;
    private double allBetSum = 0;
    private boolean open;
    private boolean active;
    private boolean allowOrder;
    private Side side;
    private Map<String, Object> parameters;

    public Strategy(Strategy strategy) {
        this.name = strategy.getName();
        this.channelId = strategy.getChannelId();
        this.instrumentName = strategy.getInstrumentName();
        this.parameters = strategy.getParameters();
        this.type = strategy.getType();
        this.slPercent = strategy.getSlPercent();
        this.tpPercent = strategy.getTpPercent();
        this.active = strategy.isActive();
        this.allowOrder = strategy.isAllowOrder();
    }

    @Override
    public String toString() {
        return STR.
                "Strategy{name='\{name
                        }, instrumentName= '\{instrumentName
                        }, channelId='\{channelId
                        }, parameters='\{parameters
                        }, getType()=\{getType()
                        }, getTpPercent()=\{getTpPercent()
                        }, getSlPercent()=\{getSlPercent()
                        }, isActive()=\{isActive()
                        }, isAllowOrder()=\{isAllowOrder()
                        }}";
    }

    public String getMessageForSendOpenPosition(double sumWithLeverage, double percent, double sum, double percentOfSum, InstrumentService is, StrategyService ss) {
        return getMessage("", sumWithLeverage, percent, sum, percentOfSum);
    }

    public String getMessageForSendClosePosition(String result, double sumWithLeverage, double percent, double sum, double percentOfSum) {
        return getMessage(result, sumWithLeverage, percent, sum, percentOfSum);
    }

    private String getMessage(String result, double sumWithLeverage, double percent, double sum, double percentOfSum) {
        String isOpenClose = isOpen() ? "#open" : "#close";

        return STR."""
        #\{getName()} #\{getSide()} \{isOpenClose}

        #\{getInstrumentName()} PO: \{getPriceOpen()} \{result}

        Ставка: \{sumWithLeverage}

        \{getInstrumentName()}: \{percent}% | \{sum}$ | \{percentOfSum}%""";
    }

    public boolean checkToOpen(ISService isService) {
        return false;
    }

    public boolean checkToClose(ISService isService) {
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
