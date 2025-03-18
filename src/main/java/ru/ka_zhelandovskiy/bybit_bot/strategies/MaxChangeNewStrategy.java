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

import java.util.List;


@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class MaxChangeNewStrategy extends Strategy {
    private List<String> blackList;
    private Double maxChangeShift;

    public MaxChangeNewStrategy(Strategy strategy) {
        super(strategy);
        this.blackList = (List<String>) strategy.getParameters().get("blackList");
        this.maxChangeShift = ((Number) strategy.getParameters().get("maxChangeShift")).doubleValue();
    }

    @Override
    public String toString() {
        return STR."MaxChangeNewStrategy{getName()=\{getName()
                }, getInstrumentName()=\{getInstrumentName()
                }, getType()=\{getType()
                }, getChannelId()=\{getChannelId()
                }, getSlPercent()=\{getSlPercent()
                }, getTpPercent()=\{getTpPercent()
                }, isActive()=\{isActive()
                }, blackList()=\{this.blackList
                }, maxChangeShift()=\{this.maxChangeShift
                }}";
    }

    @Override
    public boolean checkToOpen(ISService isService) {
        InstrumentService is = isService.getInstrumentService();
        StrategyService ss = isService.getStrategyService();

        Instrument instrument = is.getInstrumentByName(getInstrumentName());

        if (inBlackList(instrument.getSymbol()))
            return false;

        double currentPrice = instrument.getCurrentPrice();

        Candlestick cndst = instrument.getCandlestickList().getFirst();

        double priceChange = ss.getPriceChangePercent(cndst.getPriceOpen(), currentPrice);
        double maxChange = instrument.getMaxChange() + (instrument.getMaxChange() * maxChangeShift);

        boolean conditionToOpen = priceChange >= maxChange;
        log.info(STR."    priceChange >= maxChange \{Utilities.roundDouble(priceChange)} >= \{maxChange}?");


        if (conditionToOpen) {
            setPriceOpen(instrument.getCurrentPrice());

            if (getSide() == null) {
                if (currentPrice < cndst.getPriceOpen())
                    setSide(Side.BUY);
                if (currentPrice > cndst.getPriceOpen())
                    setSide(Side.SELL);
            }
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

    private boolean inBlackList(String instrument) {
        return blackList.stream()
                .anyMatch(inst -> inst.equals(instrument));
    }
}