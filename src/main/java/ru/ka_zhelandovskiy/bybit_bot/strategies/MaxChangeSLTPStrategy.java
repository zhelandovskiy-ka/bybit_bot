package ru.ka_zhelandovskiy.bybit_bot.strategies;

import com.bybit.api.client.domain.trade.Side;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.ka_zhelandovskiy.bybit_bot.dto.Candlestick;
import ru.ka_zhelandovskiy.bybit_bot.dto.Instrument;
import ru.ka_zhelandovskiy.bybit_bot.enums.SumType;
import ru.ka_zhelandovskiy.bybit_bot.services.ISService;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class MaxChangeSLTPStrategy extends Strategy {
    @Getter
    private List<String> blackList;
    private int maxCountOpen;
    private double miniTp;
    private double allQuantity = 0;
    @Getter
    private int countOpen = 0; //сколько раз сработал стоп лосс/сколько раз добавлялась позиция
    private boolean slChanged = false;
    private Map<String, Double> maxChanges;

    public MaxChangeSLTPStrategy(Strategy strategy) {
        super(strategy);
        this.blackList = (List<String>) strategy.getParameters().get("blackList");
        this.maxCountOpen = (int) strategy.getParameters().get("maxCountOpen");
        this.miniTp = (double) strategy.getParameters().get("miniTp");
        this.maxChanges = (Map<String, Double>) strategy.getParameters().get("maxChanges");
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
                }, maxChanges()=\{maxChanges
                }}";
    }

    @Override
    public boolean checkToOpen(ISService isService) {
        InstrumentService is = isService.getInstrumentService();
        StrategyService ss = isService.getStrategyService();
        Instrument instrument = is.getInstrumentByName(getInstrumentName());

//        if (inBlackList(instrument.getSymbol()))
//            return false;

        Candlestick cndst = instrument.getCandlestickList().getFirst();
        double currentPrice = instrument.getCurrentPrice();
        double priceChange = ss.getPriceChangePercent(cndst.getPriceOpen(), currentPrice);
        double maxChange;

        if (maxChanges != null) {
            maxChange = maxChanges.get(getInstrumentName());
        } else
            maxChange = instrument.getMaxChange();

        boolean conditionToOpen = priceChange >= maxChange;
        log.info(STR."    CP: \{currentPrice}");
        log.info(STR."    priceChange >= maxChange \{Utilities.roundDouble(priceChange)} >= \{maxChange}?");

        if (!conditionToOpen)
            conditionToOpen = countOpen > 0;

        if (conditionToOpen) {
            setAllBetSum(getAllBetSum() + is.getSumWithLeverage(SumType.sum, getInstrumentName()));

            if (countOpen == 0) {
                slChanged = false;
                setPriceOpen(currentPrice);
                setPreviousPriceOpen(cndst.getPriceOpen());
            }
            else {
                setPreviousPriceOpen(getPriceOpen());
                setPriceOpen((currentPrice + getPriceOpen()) / 2);
            }

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

        log.info(STR."    check TP: \{profitPercent} >= \{getTpPercent()}");
        if (profitPercent >= getTpPercent() * miniTp) {
            double newSlPercent = profitPercent - (getTpPercent() * miniTp) + 0.04;
            if (newSlPercent > getSlPercent()) {
                setSlPercent(newSlPercent);
                slChanged = true;
                log.info(STR."        SL changed: \{getSlPercent()}");
            }
        }

        boolean result = false;

        log.info(STR."    check SL: \{profitPercent} <= \{getSlPercent()}");
        if (profitPercent <= getSlPercent()) {
            if (slChanged) {
                log.info("        SL was changed");
                result = true;
            }

            if (this.countOpen == maxCountOpen) {
                log.info("        countOpen = maxCountOpen, {} = {}", countOpen, maxCountOpen);
                result = true;
            }
            else {
                setOpen(false);
            }
        } else
            result = profitPercent >= getTpPercent();

        if (result) {
            countOpen = 0;
        }

        return result;
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

        Ставка (\{countOpen}): \{sumWithLeverage} Вся ставка: \{getAllBetSum()}

        \{direction} \{Utilities.roundDouble(getPreviousPriceOpen())} -> \{Utilities.roundDouble(currentPrice)} (\{Utilities.roundDouble(profit)}%) | \{instrument.getMaxChange()}

        \{getInstrumentName()}: \{percent}% | \{sum}$ | \{percentOfSum}%""";
    }

    private boolean inBlackList(String instrument) {
        return blackList.stream()
                .anyMatch(inst -> inst.equals(instrument));
    }
}
