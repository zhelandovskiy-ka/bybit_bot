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
public class MaxChangeSLTPStrategy extends Strategy {
    private List<String> blackList;
    private int maxCountOpen;
    private double miniTp;
    private int countOpen = 0; //сколько раз сработал стоп лосс/сколько раз добавлялась позиция
    private boolean slChanged = false;

    public MaxChangeSLTPStrategy(Strategy strategy) {
        super(strategy);
        this.blackList = (List<String>) strategy.getParameters().get("blackList");
        this.maxCountOpen = (int) strategy.getParameters().get("maxCountOpen");
        this.miniTp = (double) strategy.getParameters().get("miniTp");
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
                }, maxCountSL()=\{this.maxCountOpen
                }, miniTp()=\{this.miniTp
                }}";
    }

    @Override
    public boolean checkToOpen(ISService isService) {
        InstrumentService is = isService.getInstrumentService();
        StrategyService ss = isService.getStrategyService();
        Instrument instrument = is.getInstrumentByName(getInstrumentName());

        if (inBlackList(instrument.getSymbol()))
            return false;

        Candlestick cndst = instrument.getCandlestickList().getFirst();
        double currentPrice = instrument.getCurrentPrice();
        double priceChange = ss.getPriceChangePercent(cndst.getPriceOpen(), currentPrice);
        double maxChange = instrument.getMaxChange();

        boolean conditionToOpen = priceChange >= maxChange;
        log.info(STR."    current price: \{currentPrice}");
        log.info(STR."    \{instrument.getSymbol()} | \{getName()} checkToOpen priceChange >= getMaxChange \{Utilities.roundDouble(priceChange)} >= \{maxChange}?");

        if (conditionToOpen) {
            log.info(STR."    \{getInstrumentName()} conditionToOpen is \{conditionToOpen}");

            if (getPriceOpen() == 0.0)
                setPriceOpen(instrument.getCurrentPrice());
            else
                setPriceOpen((instrument.getCurrentPrice() + getPriceOpen() / 2));

            this.countOpen += 1;

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

        if (profitPercent >= getTpPercent() * miniTp) {
            setSlPercent(0.001);
            slChanged = true;
        }

        if (profitPercent <= getSlPercent()) {
            if (slChanged)
                return true;

            if (this.countOpen == maxCountOpen)
                return true;
            else {
                setOpen(false);
            }
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

        Ставка: \{sumWithLeverage} Вся ставка: \{getAllBetSum()} №\{countOpen}

        \{direction} \{Utilities.roundDouble(getPreviousPriceOpen())} -> \{Utilities.roundDouble(currentPrice)} (\{Utilities.roundDouble(profit)}%) | \{instrument.getMaxChange()}

        \{getInstrumentName()}: \{percent}% | \{sum}$ | \{percentOfSum}%""";
    }

    private boolean inBlackList(String instrument) {
        return blackList.stream()
                .anyMatch(inst -> inst.equals(instrument));
    }
}
