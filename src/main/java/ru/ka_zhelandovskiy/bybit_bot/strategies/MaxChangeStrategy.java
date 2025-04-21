package ru.ka_zhelandovskiy.bybit_bot.strategies;

import com.bybit.api.client.domain.trade.Side;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ru.ka_zhelandovskiy.bybit_bot.dto.Candlestick;
import ru.ka_zhelandovskiy.bybit_bot.dto.Instrument;
import ru.ka_zhelandovskiy.bybit_bot.enums.SumType;
import ru.ka_zhelandovskiy.bybit_bot.services.ISService;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

import java.util.Map;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaxChangeStrategy extends Strategy {
    private double slShift;
    private double miniSL;
    private double allQuantity = 0;
    private double allPrices = 0;
    private double firstOpenPrice;
    private boolean wasOpen = false;
    private Map<String, Double> maxChanges;

    public MaxChangeStrategy(Strategy strategy) {
        super(strategy);
        this.slShift = (Double) strategy.getParameters().get("slShift");
        this.miniSL = (Double) strategy.getParameters().get("miniSL");
        this.maxChanges = (Map<String, Double>) strategy.getParameters().get("maxChanges");
    }

    @Override
    public String toString() {
        return STR."MaxChangeStrategy{getName()=\{getName()
                }, getInstrumentName()=\{getInstrumentName()
                }, getType()=\{getType()
                }, getChannelId()=\{getChannelId()
                }, slShift=\{slShift
                }, miniSL=\{miniSL
                }, getSlPercent()=\{getSlPercent()
                }, getTpPercent()=\{getTpPercent()
                }, isActive()=\{isActive()
                }, maxChanges()=\{maxChanges
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
        double maxChange;

        if (maxChanges != null) {
            maxChange = maxChanges.get(getInstrumentName());
        } else
            maxChange = instrument.getMaxChange();

        boolean conditionToOpen = priceChange >= maxChange;
        log.info(STR."    priceChange >= maxChange \{Utilities.roundDouble(priceChange)} >= \{maxChange}?");

        if (wasOpen) {
            conditionToOpen = true;
            log.info(STR."        it wasOpen = true | setPO: \{getPriceOpen()}");
        }

        if (conditionToOpen) {
            wasOpen = false;

            double quantity = Double.parseDouble(is.getQuantity(getInstrumentName(), SumType.sum));

            setAllPrices(getAllPrices() + (currentPrice * quantity));
            setAllQuantity(getAllQuantity() + quantity);
            setAllBetSum(getAllBetSum() + is.getSumWithLeverage(SumType.sum, getInstrumentName()));
            setPreviousPriceOpen(getPriceOpen());
            setPriceOpen(currentPrice);

            if (getFirstOpenPrice() == 0) {
                setFirstOpenPrice(currentPrice);
                setPreviousPriceOpen(cndst.getPriceOpen());
            }

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
        double roundProfitPercent = Utilities.roundDouble(profitPercent);

        log.info(STR."     \{getInstrumentName()} \{roundProfitPercent} <= \{miniSL}?");

        if (profitPercent <= miniSL) {
            wasOpen = true;
            setOpen(false);
            log.info(STR."        TRUE | \{getInstrumentName()} setOpen(false) wasOpen(true)");
        }

        log.info(STR."     \{getInstrumentName()} \{roundProfitPercent} >= \{getTpPercent()} && \{roundProfitPercent} > \{getSlPercent() + slShift}?");

        if (profitPercent >= getTpPercent() && profitPercent > getSlPercent() + slShift) {
            setSlPercent(profitPercent - slShift);
            log.info(STR."        TRUE | SET NEW SL PERCENT: \{roundProfitPercent} - \{slShift} = \{getSlPercent()}");
        }

        return false;
    }

    @Override
    public String getMessageForSendOpenPosition(double sumWithLeverage, double percent, double sum, double percentOfSum, InstrumentService is, StrategyService ss) {
        String isOpenClose = isOpen() ? "#open" : "#close";

        Instrument instrument = is.getInstrumentByName(getInstrumentName());
        Candlestick cndst = instrument.getCandlestickList().getFirst();

        double pavg = Utilities.roundDouble(getAllPrices() / getAllQuantity());
        double cndstPriceOpen = cndst.getPriceOpen();
        double currentPrice = instrument.getCurrentPrice();
        double profit = ss.getPriceChangePercent(cndstPriceOpen, currentPrice);

        String direction = currentPrice > cndstPriceOpen ? "⬆ " : "⬇ ";

        return STR."""
        #\{getName()} #\{getSide()} \{isOpenClose}

        #\{getInstrumentName()} PO: \{getPriceOpen()} PAVG: \{ pavg }

        Ставка: \{sumWithLeverage} Вся ставка: \{getAllBetSum()}

        AllPrices: \{Utilities.roundDouble(getAllPrices())} AllQuantity: \{getAllQuantity()}

        \{direction} \{Utilities.roundDouble(getPreviousPriceOpen())} -> \{Utilities.roundDouble(currentPrice)} (\{Utilities.roundDouble(profit)}%) | \{instrument.getMaxChange()}

        \{getInstrumentName()}: \{percent}% | \{sum}$ | \{percentOfSum}%""";
    }

    @Override
    public String getMessageForSendClosePosition(String result, double sumWithLeverage, double percent, double sum, double percentOfSum) {
        return super.getMessageForSendClosePosition(result, getAllBetSum(), percent, sum, percentOfSum);
    }

    public double getAllQuantity() {
        return Utilities.roundDouble(allQuantity);
    }

    public double getAllPrices() {
        return Utilities.roundDouble(allPrices);
    }
}