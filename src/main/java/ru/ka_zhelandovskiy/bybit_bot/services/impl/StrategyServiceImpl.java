package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import com.bybit.api.client.domain.trade.Side;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.dto.SumType;
import ru.ka_zhelandovskiy.bybit_bot.mapper.StrategyMapper;
import ru.ka_zhelandovskiy.bybit_bot.models.StrategyModel;
import ru.ka_zhelandovskiy.bybit_bot.repository.StrategyRepository;
import ru.ka_zhelandovskiy.bybit_bot.services.*;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StrategyServiceImpl implements StrategyService {
    private final StrategyRepository strategyRepository;
    private final StrategyMapper strategyMapper;
    private final ParameterService parameterService;
    private final SenderService senderService;
    private final InstrumentService instrumentService;
    private final ResultService resultService;
    private final StatisticsService statisticsService;

    @Getter
    private List<Strategy> strategyList;

    public StrategyServiceImpl(StrategyRepository strategyRepository, StrategyMapper strategyMapper, ParameterService parameterService, SenderService senderService, InstrumentService instrumentService, ResultService resultService, StatisticsService statisticsService) {
        this.strategyRepository = strategyRepository;
        this.strategyMapper = strategyMapper;
        this.parameterService = parameterService;
        this.senderService = senderService;
        this.instrumentService = instrumentService;
        this.resultService = resultService;
        this.statisticsService = statisticsService;
        this.strategyList = initStrategyList();

        System.out.println("STRATEGY LIST:");
        strategyList.forEach(System.out::println);
    }

    @Override
    public List<Strategy> initStrategyList() {
        int timeFrame = parameterService.getTimeFrame();

        System.out.println("STRATEGY MODEL LIST:");
        getAllStrategiesByTimeFrame(timeFrame).forEach(System.out::println);

        return getAllStrategiesByTimeFrame(timeFrame)
                .stream()
                .map(strategyMapper::mapModelToStrategy)
                .collect(Collectors.toList());
    }

    @Override
    public List<StrategyModel> getAllStrategiesByTimeFrame(int timeFrame) {
        return strategyRepository.findAllByTimeFrame(timeFrame);
    }

    @Override
    public Strategy getStrategyByName(String name) {
        return getStrategyList()
                .stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateStrategy(Strategy strategy) {
        for (int i = 0; i < strategyList.size(); i++) {
            if (getStrategyList().get(i).getName().equals(strategy.getName()))
                strategyList.set(i, strategy);
        }
    }

    @Override
    public void send(Strategy str) {
//        String result = generateResultMessage(str);

//        chartForm.drawAndMakeScreen(str.getInstrumentName());
//        InstrumentsController.saveData();

//        generateMessage(str, result);
        senderService.send("screen.png", str.getChannelId(), generateMessage(str));

//        statisticsService.addRecord(str);

    }

    private String generateMessage(Strategy str) {
        String result = "";

        if (!str.isOpen() && str.getProfitSum() != 0)
            result = generateResultMessage(str);

        String instrumentName = str.getInstrumentName();

        double bank = resultService.getBank(str.getName());
        double sum = statisticsService.getProfitByInstrumentAndStrategy(instrumentName, str.getName());
        double percent = statisticsService.getPercentByInstrumentAndStrategy(instrumentName, str.getName());

        double percentOfSum = Utilities.roundDouble(sum / bank * 100);

        double sumWithLeverage = instrumentService.getSumWithLeverage(SumType.sum, instrumentName);

        return str.getMessageForSend(result, sumWithLeverage, percent, sum, percentOfSum, instrumentService, this);
    }

    private String generateResultMessage(Strategy str) {
        String result = "";

        double profitWOFee = getProfitWOFee(str);
        double percentByProfit = getPercentByProfit(str);
        double profitReal = getProfitReal(str);
        double profitRealPercent = getProfitRealPercent(str);

        resultService.incrementsResult(str.getName(), profitWOFee);

        double bank = resultService.getBank(str.getName());
        double dayProfit = resultService.getDayMoney(str.getName());

        if (profitWOFee > 0)
            result = STR."PC: \{str.getPriceClose()}\n\n✅ \{profitWOFee} \{percentByProfit}% (\{profitReal} \{profitRealPercent}%)\n";
        if (profitWOFee < 0)
            result = STR."PC: \{str.getPriceClose()}\n\n❌ \{profitWOFee} \{percentByProfit}% (\{profitReal} \{profitRealPercent}%)\n";
        if (profitWOFee == 0)
            result = STR."PC: \{str.getPriceClose()}♻\n";

        int leverage = instrumentService.getLeverageBySymbol(str.getInstrumentName());

        result = result
                + getStatString("-", str.getLoseMax(), leverage)
                + getStatString("+", str.getProfitMax(), leverage)
                + STR."\n\nБанк: \{bank}$ (\{dayProfit}$)";

        return result;
    }

    private String getStatString(String type, double loseProfitMax, int leverage) {
        return STR."\nМакс \{type} : \{loseProfitMax} (\{Utilities.roundDouble(loseProfitMax * leverage)})";
    }

    @Override
    public double getProfitPercent(Strategy s) {
        s.setProfitPercent(0);

        double currentPrice = instrumentService.getCurrentPrice(s.getInstrumentName());
        double priceChange = 0;

        if (s.getSide() == Side.BUY)
            priceChange = currentPrice - s.getPriceOpen();

        if (s.getSide() == Side.SELL)
            priceChange = s.getPriceOpen() - currentPrice;

        s.setProfitPercent(priceChange / s.getPriceOpen() * 100);

        log.info(STR."     getProfitPercent 1 | getSide: \{s.getSide()} currentPrice: \{currentPrice} getPriceOpen: \{s.getPriceOpen()} ProfitPercent: \{priceChange} / \{s.getPriceOpen()} * 100 = \{s.getProfitPercent()}");

        return s.getProfitPercent();
    }

    @Override
    public double getProfitPercent(Strategy s, double priceOpen) {
        double currentPrice = instrumentService.getCurrentPrice(s.getInstrumentName());
        double priceChange = 0;

        if (s.getSide() == Side.BUY)
            priceChange = currentPrice - priceOpen;

        if (s.getSide() == Side.SELL)
            priceChange = priceOpen - currentPrice;

        double profitPercent = priceChange / s.getPriceOpen() * 100;
//2024-03-21 12:04:50 -    getProfitPercent 2 | getSide: SELL currentPrice: 3086.3 firstOpenPrice: 3085.3 ProfitPercent: -1.0 / 3085.3 * 100 = -0.03241175898616018
        log.info(STR."     getProfitPercent 2 | getSide: \{s.getSide()} currentPrice: \{currentPrice} firstOpenPrice: \{priceOpen} ProfitPercent: \{priceChange} / \{s.getPriceOpen()} * 100 = \{profitPercent}");

        return profitPercent;
    }

    @Override
    public double getPriceChangePercent(double priceOpen, double price) {
        log.info(STR."    cndst.priceOpen: \{priceOpen} curPrice: \{price}");
        double profit = (price - priceOpen) / priceOpen * 100;

        return profit < 0 ? profit * -1 : profit;
    }

    @Override
    public void calcProfitSum(Strategy s) {
        double sum = s.getAllBetSum();
        if (s.getSide() == Side.BUY)
            s.setProfitSum((s.getPriceClose() - s.getPriceOpen()) * (sum / s.getPriceOpen()));
        if (s.getSide() == Side.SELL)
            s.setProfitSum((s.getPriceOpen() - s.getPriceClose()) * (sum / s.getPriceOpen()));
        s.setAllBetSum(0);
    }

    @Override
    public void calcMaxProfitLosePercent(Strategy str) {
        double profit = getProfitPercent(str);

        if (profit > 0) {
            str.setProfitMax(Math.max(profit, str.getProfitMax()));
        }
        if (profit < 0) {
            str.setLoseMax(Math.min(profit, str.getLoseMax()));
        }
    }

    @Override
    public void resetSLTPPercent(Strategy str) {
        str.setSlPercent(strategyRepository.findByName(str.getName()).getSlPercent());
        str.setTpPercent(strategyRepository.findByName(str.getName()).getTpPercent());
    }

    @Override
    public void resetSide(Strategy str) {
        str.setSide(null);
    }

    //    @Override
    private double getProfitPercentWithLeverageWoFee(Strategy s, double leverage) {
        double fee = parameterService.getFee();

        return s.getProfitPercent() * leverage - (fee * 2 * 100);
    }

    //    @Override
    private double getProfitWOFee(Strategy strategy) {
        log.info(STR."getProfitWOFee: \{strategy.getProfitSum()} - \{instrumentService.getSumOfFee(strategy.getInstrumentName())}");

        return Utilities.roundDouble(strategy.getProfitSum() - instrumentService.getSumOfFee(strategy.getInstrumentName()), 2);
    }

    //    @Override
    private double getPercentByProfit(Strategy strategy) {
        log.info(STR."getPercentByProfit: \{getProfitWOFee(strategy)} / \{parameterService.getSum() * 100}");

        return Utilities.roundDouble(getProfitWOFee(strategy) / parameterService.getSum() * 100);
    }

    //    @Override
    private double getProfitReal(Strategy strategy) {
        return Utilities.roundDouble((getProfitWOFee(strategy) / (parameterService.getSum() / parameterService.getRealSum())));
    }

    //    @Override
    private double getProfitRealPercent(Strategy strategy) {
        return Utilities.roundDouble(getProfitReal(strategy) / parameterService.getRealSum() * 100);
    }
}
