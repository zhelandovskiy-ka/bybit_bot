package ru.ka_zhelandovskiy.bybit_bot.strategies;

import com.bybit.api.client.domain.trade.Side;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.ka_zhelandovskiy.bybit_bot.dto.Candlestick;
import ru.ka_zhelandovskiy.bybit_bot.dto.Instrument;
import ru.ka_zhelandovskiy.bybit_bot.services.ISService;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class ScalpMinMaxVolStrategy extends Strategy {
    private boolean reverse;

    public ScalpMinMaxVolStrategy(Strategy strategy) {
        super(strategy);
        this.reverse = (Boolean) strategy.getParameters().get("reverse");
    }

    @Override
    public String toString() {
        return STR."ScalpMinMaxVolStrategy{getName()=\{getName()
                }, getType()=\{getType()
                }, getInstrumentName()=\{getInstrumentName()
                }, getChannelId()=\{getChannelId()
                }, getSlPercent()=\{getSlPercent()
                }, getTpPercent()=\{getTpPercent()
                }, reverse()=\{this.reverse
                }, isActive()=\{isActive()
                }}";
    }

    @Override
    public boolean checkToOpen(ISService isService) {
        InstrumentService is = isService.getInstrumentService();

        Instrument instrument = is.getInstrumentByName(getInstrumentName());
        double currentPrice = instrument.getCurrentPrice();
        Candlestick cndst = instrument.getCurrentCandlestick();
        Candlestick prevCndst = instrument.getPrevCandlestick();

        double maxPosition = instrument.getMaxPriceHigh(0.9999);
        double minPosition = instrument.getMinPriceLow(0.9999);
        double maxOverPosition = instrument.getMaxPriceHigh(1.0002);
        double minOverPosition = instrument.getMinPriceLow(1.0002);
        double currentVolume = instrument.getCurrentVolume();
        double prevVolume = instrument.getPrevVolume();
        double prevPrice = instrument.getPrevPriceClose();
        double avgVolume = instrument.getAvgVolume();

        log.info("    {}", cndst.toString());
        log.info("    {}", prevCndst.toString());

        boolean conditionToOpen = false;

        if (currentPrice > maxPosition && currentVolume > (avgVolume * 2)) {
            conditionToOpen = true;
            if (reverse)
                setSide(Side.BUY);
            else
                setSide(Side.SELL);
        }

       if (currentPrice < minPosition && currentVolume > (avgVolume * 2)) {
            conditionToOpen = true;
            if (reverse)
                setSide(Side.SELL);
            else
                setSide(Side.BUY);
        }

        if (currentPrice > maxOverPosition
                && prevPrice > instrument.getMaxPriceClose()
                && (prevVolume > (avgVolume * 2) || currentVolume > (avgVolume * 2))) {

            conditionToOpen = true;
            if (reverse)
                setSide(Side.SELL);
            else
                setSide(Side.BUY);
        }

        if (currentPrice < minOverPosition
                && prevPrice < instrument.getMinPriceClose()
                && (prevVolume > (avgVolume * 2) || currentVolume > (avgVolume * 2))) {

            conditionToOpen = true;
            if (reverse)
                setSide(Side.BUY);
            else
                setSide(Side.SELL);
        }

        if (conditionToOpen) {
            setPriceOpen(instrument.getCurrentPrice());
        }

        return conditionToOpen;
    }

    @Override
    public boolean checkToClose(ISService isService) {
        StrategyService ss = isService.getStrategyService();

        double profitPercent = ss.getProfitPercent(this);

        log.info(STR."    check SL: \{profitPercent} <= \{getSlPercent()}");

        if (profitPercent <= getSlPercent()) {
            return true;
        }

        log.info(STR."    check TP: \{profitPercent} >= \{getTpPercent()}");
        if (profitPercent >= getTpPercent()) {
            return true;
        }

        return false;
    }

    @Override
    public String getMessageForSendOpenPosition(double sumWithLeverage, double percent, double sum, double percentOfSum, InstrumentService is, StrategyService ss) {
        String isOpenClose = isOpen() ? "#open" : "#close";

        Instrument instrument = is.getInstrumentByName(getInstrumentName());
        Candlestick cndst = instrument.getCandlestickList().getFirst();

        double cndstPriceOpen = cndst.getPriceOpen();
        double currentPrice = instrument.getCurrentPrice();
        double profit = ss.getPriceChangePercent(cndstPriceOpen, currentPrice);

        String direction = currentPrice > cndstPriceOpen ? "⬆ " : "⬇ ";

        return STR."""
        #\{getName()} #\{getSide()} \{isOpenClose}

        #\{getInstrumentName()} PO: \{getPriceOpen()}

        Ставка: \{sumWithLeverage} Вся ставка: \{getAllBetSum()}

        \{direction} \{Utilities.roundDouble(getPreviousPriceOpen())} -> \{Utilities.roundDouble(currentPrice)} (\{Utilities.roundDouble(profit)}%) | \{instrument.getMaxChange()}

        \{getInstrumentName()}: \{percent}% | \{sum}$ | \{percentOfSum}%""";
    }
}
