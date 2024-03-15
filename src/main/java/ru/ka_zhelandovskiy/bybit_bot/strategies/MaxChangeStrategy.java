package ru.ka_zhelandovskiy.bybit_bot.strategies;

import com.bybit.api.client.domain.trade.Side;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.ka_zhelandovskiy.bybit_bot.dto.Candlestick;
import ru.ka_zhelandovskiy.bybit_bot.dto.Instrument;
import ru.ka_zhelandovskiy.bybit_bot.dto.SumType;
import ru.ka_zhelandovskiy.bybit_bot.services.ISService;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

@Data
@Slf4j
public class MaxChangeStrategy extends Strategy {
    private double allQuantity = 0;
    private double allPrices = 0;
    private double slShift;
    private double miniSL;
    private double firstOpenPrice;
    private boolean wasOpen = false;

    @Override
    public String toString() {
        return STR."MaxChangeStrategy{getName()=\{getName()
                }, getInstrumentName()=\{getInstrumentName()
                }, getChannelId()=\{getChannelId()
                }, slShift=\{slShift
                }, miniSL=\{miniSL
                }, getSlPercent()=\{getSlPercent()
                }, getTpPercent()=\{getTpPercent()
                }, isActive()=\{isActive()
                }}";
    }

    public MaxChangeStrategy(Strategy strategy) {
        super(strategy);
        this.slShift = (Double) strategy.getParameters().get("slShift");
        this.miniSL = (Double) strategy.getParameters().get("miniSL");
    }

    @Override
    public boolean checkToOpen(ISService isService) {
        InstrumentService is = isService.getInstrumentService();
        StrategyService ss = isService.getStrategyService();

        Instrument instrument = is.getInstrumentByName(getInstrumentName());
        double currentPrice = instrument.getCurrentPrice();

        Candlestick cndst = instrument.getCandlestickList().getFirst();

        double priceChange = ss.getPriceChangePercent(cndst.getPriceOpen(), currentPrice);
        double maxChange = instrument.getMaxChange();

        boolean conditionToOpen = priceChange >= maxChange;
        log.info(STR."    \{instrument.getSymbol()} | \{getName()} checkToOpen priceChange >= getMaxChange \{Utilities.roundDouble(priceChange)} >= \{maxChange}?");

        if (wasOpen) {
            conditionToOpen = true;
//            setPriceOpen(getAllPrices() / getAllQuantity());
            log.info(STR."        it wasOpen = true | setPriceOpen: \{getPriceOpen()}");
        }

        if (conditionToOpen) {
            wasOpen = false;

            double quantity = Double.parseDouble(is.getQuantity(getInstrumentName()));

            log.info(STR."    \{getInstrumentName()} conditionToOpen is \{conditionToOpen}");
            log.info(STR."    \{getInstrumentName()} \{getAllPrices()}, \{getAllQuantity()}, \{getAllBetSum()} (getAllPrices(), getAllQuantity(), getAllBetSum())");

            setAllPrices(getAllPrices() + (currentPrice * quantity));
            setAllQuantity(getAllQuantity() + quantity);
            setAllBetSum(getAllBetSum() + is.getSumWithLeverage(SumType.sum, getInstrumentName()));
            setPriceOpen(instrument.getCurrentPrice());

            if (firstOpenPrice == 0)
                firstOpenPrice = currentPrice;

            log.info(STR."    \{getInstrumentName()} \{getAllPrices()}, \{getAllQuantity()}, \{getAllBetSum()} (getAllPrices(), getAllQuantity(), getAllBetSum())");

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
//        InstrumentService is = isService.getInstrumentService();

//        double currentPrice = is.getInstrumentByName(getInstrumentName()).getCurrentPrice();

        double profitPercent = ss.getProfitPercent(this);
        double priceChangePercentStart = ss.getProfitPercent(this, firstOpenPrice);

        log.info(STR."     \{getInstrumentName()} \{getName()} checkToClose getPriceChangePercent() <= getSlPercent()? \{
                Utilities.roundDouble(priceChangePercentStart)} <= \{getSlPercent()} TP: \{getTpPercent()}");

        if (priceChangePercentStart <= getSlPercent()) {
            setPriceOpen(getAllPrices() / getAllQuantity());

            log.info(STR."        TRUE getProfitPercent() <= getSlPercent() | SET PRICE OPEN: \{getPriceOpen()}");

            setAllPrices(0);
            setAllQuantity(0);
            setPriceOpen(0);
            setFirstOpenPrice(0);

            return true;
        }

        log.info(STR."     \{getInstrumentName()} \{profitPercent} <= \{miniSL}?");

        if (profitPercent <= miniSL) {
            wasOpen = true;
            setOpen(false);
            log.info(STR."        TRUE | \{getInstrumentName()} setOpen(false) wasOpen(true)");
        }

        log.info(STR."     \{getInstrumentName()} \{profitPercent} >= \{getTpPercent()} && \{profitPercent} > \{getSlPercent() + slShift}?");

        if (profitPercent >= getTpPercent() && profitPercent > getSlPercent() + slShift) {
            setSlPercent(profitPercent - slShift);
            log.info(STR."        TRUE | SET NEW SL PERCENT \{getSlPercent()}");
        }

        return false;
    }

    @Override
    public String getMessageForSend(String result, double sumWithLeverage, double percent, double sum, double percentOfSum, InstrumentService is, StrategyService ss) {
        String isOpenClose = isOpen() ? "#open" : "#close";

        Instrument instrument = is.getInstrumentByName(getInstrumentName());
        Candlestick cndst = instrument.getCandlestickList().getFirst();

        double pavg = Utilities.roundDouble(getAllPrices() / getAllQuantity());
        double currentPrice = instrument.getCurrentPrice();
        double profit = ss.getPriceChangePercent(pavg, currentPrice);

        String direction = currentPrice > cndst.getPriceOpen() ? "⬆ " : "⬇ ";

        return STR."""
        #\{getName()} #\{getSide()} \{isOpenClose}

        #\{getInstrumentName()} PO: \{getPriceOpen()} PAVG: \{ pavg } \{result}

        Ставка: \{sumWithLeverage} Вся ставка: \{getAllBetSum()}

        AllPrices: \{Utilities.roundDouble(getAllPrices())} AllQuantity: \{getAllQuantity()}

        \{direction} \{Utilities.roundDouble(pavg)} -> \{Utilities.roundDouble(currentPrice)} (\{Utilities.roundDouble(profit)}%)

        \{getInstrumentName()}: \{percent}% | \{sum}$ | \{percentOfSum}%""";
    }

    public double getAllQuantity() {
        return Utilities.roundDouble(allQuantity);
    }

    public double getAllPrices() {
        return Utilities.roundDouble(allPrices);
    }
}
