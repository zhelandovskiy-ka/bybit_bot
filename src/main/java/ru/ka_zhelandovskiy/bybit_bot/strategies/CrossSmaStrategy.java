package ru.ka_zhelandovskiy.bybit_bot.strategies;

import com.bybit.api.client.domain.trade.Side;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.ka_zhelandovskiy.bybit_bot.dto.Instrument;
import ru.ka_zhelandovskiy.bybit_bot.services.ISService;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;

@Data
@Slf4j
public class CrossSmaStrategy extends Strategy {
    private int minSma;
    private int maxSma;
    private boolean reverse;

    @Override
    public String toString() {
        return STR."MaxChangeStrategy{getName()=\{getName()
                }, getInstrumentName()=\{getInstrumentName()
                }, getChannelId()=\{getChannelId()
                }, getSlPercent()=\{getSlPercent()
                }, getTpPercent()=\{getTpPercent()
                }, minSma()=\{this.minSma
                }, maxSma()=\{this.maxSma
                }, reverse()=\{this.reverse
                }, isActive()=\{isActive()
                }}";
    }

    public CrossSmaStrategy(Strategy strategy) {
        super(strategy);
        this.minSma = (Integer) strategy.getParameters().get("minSma");
        this.maxSma = (Integer) strategy.getParameters().get("maxSma");
        this.reverse = (Boolean) strategy.getParameters().get("reverse");
    }

    @Override
    public boolean checkToOpen(ISService isService) {
        InstrumentService is = isService.getInstrumentService();

        Instrument instrument = is.getInstrumentByName(getInstrumentName());

        double smaMinValuePrev = instrument.getSMAWithShift(minSma, 2);
        double smaMaxValuePrev = instrument.getSMAWithShift(maxSma, 2);

        double smaMinValue = instrument.getPrevSMA(minSma);
        double smaMaxValue = instrument.getPrevSMA(maxSma);

        boolean crossUp = smaMinValuePrev < smaMaxValuePrev && smaMinValue > smaMaxValue;
        boolean crossDown = smaMinValuePrev > smaMaxValuePrev && smaMinValue < smaMaxValue;

        boolean conditionToOpen = crossUp || crossDown;

        if (conditionToOpen) {
            if (crossUp)
                setSide(reverse ? Side.SELL : Side.BUY);
            if (crossDown)
                setSide(reverse ? Side.BUY : Side.SELL);

            setPriceOpen(instrument.getCurrentPrice());
        }

        return conditionToOpen;
    }

    @Override
    public boolean checkToClose(ISService isService) {
        InstrumentService is = isService.getInstrumentService();

        Instrument instrument = is.getInstrumentByName(getInstrumentName());

        double smaMinValuePrev = instrument.getSMAWithShift(minSma,2);
        double smaMaxValuePrev = instrument.getSMAWithShift(maxSma,2);

        double smaMinValue = instrument.getPrevSMA(minSma);
        double smaMaxValue = instrument.getPrevSMA(maxSma);

        boolean crossUp = smaMinValuePrev < smaMaxValuePrev && smaMinValue > smaMaxValue;
        boolean crossDown = smaMinValuePrev > smaMaxValuePrev && smaMinValue < smaMaxValue;

        if ((crossUp && !reverse) || (crossDown && reverse))
            return getSide() == Side.SELL;

        if ((crossUp && reverse) || (crossDown && !reverse))
            return getSide() == Side.BUY;

        return false;
    }

    @Override
    public String getMessageForSendOpenPosition(double sumWithLeverage, double percent, double sum, double percentOfSum, InstrumentService is, StrategyService ss) {
        return STR."""
        #\{getName()} #\{getSide()} #open
        #\{getInstrumentName()} PO: \{getPriceOpen()}
        \{getInstrumentName()}: \{percent}% | \{sum}$ | \{percentOfSum}%""";
    }

    @Override
    public String getMessageForSendClosePosition(String result, double sumWithLeverage, double percent, double sum, double percentOfSum) {
        return STR."""
        #\{getName()} #\{getSide()} #close
        #\{getInstrumentName()} PO: \{getPriceOpen()} \{result}
        \{percent}% | \{sum}$ | \{percentOfSum}%""";
    }
}
