package ru.ka_zhelandovskiy.bybit_bot.strategies;

import com.bybit.api.client.domain.trade.Side;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@AllArgsConstructor
@NoArgsConstructor
//@JsonTypeName("maxChangeSimple")
public class MaxChangeSimpleStrategy extends Strategy {
    private double allQuantity = 0;
    private double allPrices = 0;
    private double shift;
    private double slShift;
    private double miniSL;
    private double firstOpenPrice;
    private boolean wasOpen = false;

    public MaxChangeSimpleStrategy(Strategy strategy) {
        super(strategy);
        this.slShift = (Double) strategy.getParameters().get("slShift");
        this.miniSL = (Double) strategy.getParameters().get("miniSL");
        this.shift = (Double) strategy.getParameters().get("shift");
        setType(strategy.getType());
    }

    @Override
    public String toString() {
        return STR."MaxChangeSimpleStrategy{getName()=\{getName()
                }, getInstrumentName()=\{getInstrumentName()
                }, getType()=\{getType()
                }, getChannelId()=\{getChannelId()
                }, slShift=\{getShift()
                }, slShift=\{getSlShift()
                }, miniSL=\{getMiniSL()
                }, getSlPercent()=\{getSlPercent()
                }, getTpPercent()=\{getTpPercent()
                }, isActive()=\{isActive()
                }}";
    }

    @Override
    public boolean checkToOpen(ISService isService) {
        InstrumentService is = isService.getInstrumentService();
        StrategyService ss = isService.getStrategyService();

        Instrument instrument = is.getInstrumentByName(getInstrumentName());
        double currentPrice = instrument.getCurrentPrice();

        Candlestick cndst = instrument.getCandlestickList().getFirst();

        double priceChange = ss.getPriceChangePercent(cndst.getPriceOpen(), currentPrice);
        double maxChangeShift = instrument.getMaxChange() * shift;
        double maxChange = instrument.getMaxChange() + maxChangeShift;

        boolean conditionToOpen = priceChange >= maxChange;
        log.info(STR."    current price: \{currentPrice}");
        log.info(STR."    \{instrument.getSymbol()} | \{getName()} checkToOpen priceChange >= getMaxChange \{Utilities.roundDouble(priceChange)} >= \{maxChange}?");

        if (wasOpen) {
            conditionToOpen = true;
//            setPriceOpen(getAllPrices() / getAllQuantity());
            log.info(STR."        it wasOpen = true | setPriceOpen: \{getPriceOpen()}");
        }

        if (conditionToOpen) {
            wasOpen = false;

            double quantity = Double.parseDouble(is.getQuantity(getInstrumentName(), SumType.sum));

            log.info(STR."    \{getInstrumentName()} conditionToOpen is \{conditionToOpen}");
            log.info(STR."    \{getInstrumentName()} \{getAllPrices()}, \{getAllQuantity()}, \{getAllBetSum()} (getAllPrices(), getAllQuantity(), getAllBetSum())");

            setAllPrices(getAllPrices() + (currentPrice * quantity));
            setAllQuantity(getAllQuantity() + quantity);
            setAllBetSum(getAllBetSum() + is.getSumWithLeverage(SumType.sum, getInstrumentName()));
            setPreviousPriceOpen(getPriceOpen());
            setPriceOpen(instrument.getCurrentPrice());

            if (getFirstOpenPrice() == 0) {
                setFirstOpenPrice(currentPrice);
                setPreviousPriceOpen(cndst.getPriceOpen());
            }

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

        double priceChangePercentStart = ss.getProfitPercent(this, firstOpenPrice);

        log.info(STR."     \{getInstrumentName()} \{getName()} checkToClose priceChangePercentStart <= getSlPercent()? \{
                Utilities.roundDouble(priceChangePercentStart)} <= \{getSlPercent()} TP: \{getTpPercent()}");

        if (priceChangePercentStart <= getSlPercent()) {
            setPriceOpen(getAllPrices() / getAllQuantity());

            log.info(STR."        TRUE | SET PRICE OPEN: \{getAllPrices()} / \{getAllQuantity()} = \{getPriceOpen()}");

            setAllPrices(0);
            setAllQuantity(0);
            setFirstOpenPrice(0);
            setPreviousPriceOpen(0);

            return true;
        }

        double profitPercent = ss.getProfitPercent(this);

        log.info(STR."     \{getInstrumentName()} \{profitPercent} <= \{miniSL}?");

        if (profitPercent <= miniSL) {
            wasOpen = true;
            setOpen(false);
            log.info(STR."        TRUE | \{getInstrumentName()} setOpen(false) wasOpen(true)");
        }

        log.info(STR."     \{getInstrumentName()} \{priceChangePercentStart} >= \{getTpPercent()} && \{priceChangePercentStart} > \{getSlPercent() + slShift}?");

        if (priceChangePercentStart >= getTpPercent() && priceChangePercentStart > getSlPercent() + slShift) {
            setSlPercent(priceChangePercentStart - slShift);
            log.info(STR."        TRUE | SET NEW SL PERCENT: \{priceChangePercentStart} - \{slShift} = \{getSlPercent()}");
        }

        return false;
    }

    @Override
    public String getMessageForSendOpenPosition(double sumWithLeverage, double percent, double sum, double percentOfSum, InstrumentService is, StrategyService ss) {
        double pavg = Utilities.roundDouble(getAllPrices() / getAllQuantity());

        return STR."""
        #\{getName()} #\{getSide()} #open
        #\{getInstrumentName()} PO: \{getPriceOpen()} PAVG: \{ pavg }
        \{getInstrumentName()}: \{percent}% | \{sum}$ | \{percentOfSum}%""";
    }

    @Override
    public String getMessageForSendClosePosition(String result, double sumWithLeverage, double percent, double sum, double percentOfSum) {
        return STR."""
        #\{getName()} #\{getSide()} #close
        #\{getInstrumentName()} PO: \{getPriceOpen()} \{result}
        \{percent}% | \{sum}$ | \{percentOfSum}%""";
    }

    public double getAllQuantity() {
        return Utilities.roundDouble(allQuantity);
    }

    public double getAllPrices() {
        return Utilities.roundDouble(allPrices);
    }
}
