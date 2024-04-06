
package ru.ka_zhelandovskiy.bybit_bot.strategies;

import com.bybit.api.client.domain.trade.Side;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ka_zhelandovskiy.bybit_bot.services.ISService;
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
    private Side side;
    private Map<String, Object> parameters;

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
        this.active = strategy.isActive();
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
                        }}";
    }

    public String getMessageForSend(String result, double sumWithLeverage, double percent, double sum, double percentOfSum, InstrumentService is, StrategyService ss) {
        String isOpenClose = isOpen() ? "#open" : "#close";

        return STR."""
        #\{getName()} #\{getSide()} \{isOpenClose}

        #\{getInstrumentName()} PO: \{getPriceOpen()} \{result}

        Ставка: \{sumWithLeverage}

        \{getInstrumentName()}: \{percent}% | \{sum}$ | \{percentOfSum}%""";
    }


    public boolean checkToOpen(ISService isService) {
        return false;
    };

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
