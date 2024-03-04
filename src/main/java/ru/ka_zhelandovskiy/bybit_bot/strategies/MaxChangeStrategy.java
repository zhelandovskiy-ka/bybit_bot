package ru.ka_zhelandovskiy.bybit_bot.strategies;

import com.bybit.api.client.domain.trade.Side;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.ka_zhelandovskiy.bybit_bot.dto.Candlestick;
import ru.ka_zhelandovskiy.bybit_bot.dto.Instrument;
import ru.ka_zhelandovskiy.bybit_bot.dto.SumType;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

@Data
@Slf4j
public class MaxChangeStrategy extends Strategy {
    private double allQuantity = 0;
    private double allPrices = 0;
    private boolean wasOpen = false;
    private double slShift;
    private double miniSL;
    private double maxChange;

    @Override
    public String toString() {
        return STR."MaxChangeStrategy{getName()=\{getName()
                }, getInstrumentName()=\{getInstrumentName()
                }, getChannelId()=\{getChannelId()
//                }, getParameters() =\{getParameters()
                }, maxChange=\{maxChange
                }, slShift=\{slShift
                }, miniSL=\{miniSL
                }, getSlPercent()=\{getSlPercent()
                }, getTpPercent()=\{getTpPercent()
                }\{'}'}";
    }

    public MaxChangeStrategy(Strategy strategy) {
        super(strategy);
        this.slShift = (Double) strategy.getParameters().get("slShift");
        this.miniSL = (Double) strategy.getParameters().get("miniSL");
        this.maxChange = (Double) strategy.getParameters().get("maxChange");
    }

    @Override
    public boolean checkToOpen(InstrumentService is, StrategyService ss) {
        Instrument instrument = is.getInstrumentByName(getInstrumentName());
        double currentPrice = instrument.getCurrentPrice();

        Candlestick cndst = instrument.getCandlestickList().get(0);

        double profit = ss.getProfitPercent(cndst.getPriceOpen(), currentPrice);
        double maxChange = (double) getParameters().get("maxChange");

        boolean conditionToOpen = profit >= maxChange;
        log.info(STR."    \{instrument.getSymbol()} StrategyMaxChange check conditionToOpen profit>=getMaxChange \{Utilities.roundDouble(profit)} >= \{maxChange}");

        if (wasOpen) {
            conditionToOpen = true;
            setPriceOpen(getAllPrices() / getAllQuantity());
            log.info(STR."    setPriceOpen: \{getAllPrices() / getAllQuantity()}");
        }

//        if (true) {
        if (conditionToOpen) {
            wasOpen = false;

            double quantity = Double.parseDouble(is.getQuantity(getInstrumentName()));

            log.info(STR."    \{getInstrumentName()} getAllPrices(), getAllQuantity(), getAllBetSum():\{
                    getAllPrices()}, \{getAllQuantity()}, \{getAllBetSum()} condOp: \{conditionToOpen} wasOpen: \{wasOpen}");

            setAllPrices(getAllPrices() + (currentPrice * quantity));
            setAllQuantity(getAllQuantity() + quantity);
            setAllBetSum(getAllBetSum() + is.getSumWithLeverage(SumType.sum, getInstrumentName()));

            log.info(STR."    \{getInstrumentName()} getAllPrices(), getAllQuantity(), getAllBetSum(): \{getAllPrices()}, \{getAllQuantity()}, \{getAllBetSum()}");

            if (getSide() == null) {
                if (currentPrice < cndst.getPriceOpen())
                    setSide(Side.BUY);
                if (currentPrice > cndst.getPriceOpen())
                    setSide(Side.SELL);
            }
        }

//        return false;
        return conditionToOpen;
    }

    @Override
    public boolean checkToClose(InstrumentService is, StrategyService ss) {
        double profitPercent = ss.getProfitPercent(this);

        log.info(STR."     \{getInstrumentName()} \{getName()} check conditionToClose getProfitPercent() <= getSlPercent() \{
                Utilities.roundDouble(profitPercent)} <= \{Utilities.roundDouble(getSlPercent())} SL: \{getSlPercent()} TP: \{getTpPercent()}");

        if (profitPercent <= getSlPercent()) {
            log.info("        getProfitPercent() <= getSlPercent() true");

            setPriceOpen(getAllPrices() / getAllQuantity());

            setAllPrices(0);
            setAllQuantity(0);
            return true;
        }

        log.info(STR."     \{getInstrumentName()} \{profitPercent} <= \{miniSL}");

        if (profitPercent <= miniSL) {
            log.info(STR."        \{getInstrumentName()} setOpen(false)");
            wasOpen = true;
            setOpen(false);
        }

        log.info(STR."     \{getInstrumentName()} \{profitPercent} >= \{getTpPercent()} && \{profitPercent} > \{getSlPercent() + slShift}");

        if (profitPercent >= getTpPercent() && profitPercent > getSlPercent() + slShift) {
            setSlPercent(profitPercent - slShift);
            log.info(STR."\{getInstrumentName()}        set new sl percent \{getSlPercent()}");
        }

        return false;
    }

    @Override
    public String getMessageForSend(String result, double sumWithLeverage, double percent, double sum, double percentOfSum) {
        String isOpenClose = isOpen() ? "#open" : "#close";

        double pavg = Utilities.roundDouble(getAllPrices() / getAllQuantity());

        return STR."""
        #\{getName()} #\{getSide()} \{isOpenClose}

        #\{getInstrumentName()} PO: \{getPriceOpen()} PAVG: \{ pavg } \{result}

        Ставка: \{sumWithLeverage} Вся ставка: \{getAllBetSum()}

        AllPrices: \{Utilities.roundDouble(getAllPrices())} AllQuantity: \{getAllQuantity()}

        \{getInstrumentName()}: \{percent}% | \{sum}$ | \{percentOfSum}%""";
    }

    public double getAllQuantity() {
        return Utilities.roundDouble(allQuantity);
    }

    public double getAllPrices() {
        return Utilities.roundDouble(allPrices);
    }
}
