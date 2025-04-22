package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import com.bybit.api.client.domain.trade.Side;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.dto.StrategyInfoDto;
import ru.ka_zhelandovskiy.bybit_bot.enums.SumType;
import ru.ka_zhelandovskiy.bybit_bot.mapper.ResultMapper;
import ru.ka_zhelandovskiy.bybit_bot.mapper.StrategyMapper;
import ru.ka_zhelandovskiy.bybit_bot.models.ResultsModel;
import ru.ka_zhelandovskiy.bybit_bot.models.StrategyModel;
import ru.ka_zhelandovskiy.bybit_bot.repository.StrategyRepository;
import ru.ka_zhelandovskiy.bybit_bot.services.InstrumentService;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;
import ru.ka_zhelandovskiy.bybit_bot.services.ResultService;
import ru.ka_zhelandovskiy.bybit_bot.services.SenderService;
import ru.ka_zhelandovskiy.bybit_bot.services.StatisticsService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;
import ru.ka_zhelandovskiy.bybit_bot.strategies.Strategy;
import ru.ka_zhelandovskiy.bybit_bot.utils.Utilities;

import java.util.ArrayList;
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
    private final ResultMapper resultMapper;

    @Getter
    private List<Strategy> strategyList;

    public StrategyServiceImpl(StrategyRepository strategyRepository, StrategyMapper strategyMapper, ParameterService parameterService, SenderService senderService, InstrumentService instrumentService, ResultService resultService, StatisticsService statisticsService, ResultMapper resultMapper) {
        this.strategyRepository = strategyRepository;
        this.strategyMapper = strategyMapper;
        this.parameterService = parameterService;
        this.senderService = senderService;
        this.instrumentService = instrumentService;
        this.resultService = resultService;
        this.statisticsService = statisticsService;
        this.resultMapper = resultMapper;
        this.strategyList = initStrategyList();

        System.out.println("STRATEGY LIST:");
        strategyList.forEach(System.out::println);
    }

    @Override
    public List<Strategy> initStrategyList() {
        int timeFrame = parameterService.getTimeFrame();

        System.out.println("STRATEGY MODEL LIST:");
        List<StrategyModel> allStrategiesByTimeFrame = getActiveStrategiesByTimeFrame(timeFrame);
        allStrategiesByTimeFrame.forEach(strategyModel -> {
            resultService.getOrInsert(strategyModel.getName());
            System.out.println(strategyModel);
        });

        return allStrategiesByTimeFrame
                .stream()
                .map(strategyMapper::mapModelToStrategy)
                .collect(Collectors.toList());
    }

    @Override
    public List<StrategyModel> getActiveStrategiesByTimeFrame(int timeFrame) {
        return strategyRepository.findAllByTimeFrameAndActiveTrue(timeFrame);
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
    public List<String> getStrategiesNameActive() {
        return strategyRepository.findByActiveTrue().stream()
                .map(StrategyModel::getName)
                .sorted()
                .toList();
    }

    @Override
    public boolean isOpenedStrategy() {

        return strategyList.stream().anyMatch(Strategy::isOpen);
    }

    @Override
    public List<StrategyInfoDto> getStrategiesInfo() {
        List<ResultsModel> results = resultService.getAllResult();

        List<ResultsModel> filteredResults = new ArrayList<>();

        getStrategiesNameActive().forEach(name -> {
            results.forEach(result -> {
                if (result.getName().equals(name))
                    filteredResults.add(result);
            });
        });

        List<StrategyInfoDto> list = filteredResults.stream()
                .map(resultMapper::toStrategyInfoDto)
                .toList();

        list.forEach(strategyInfoDto -> {
            Strategy strategy = getStrategyByName(strategyInfoDto.getName());
            strategyInfoDto.setTpPercent(strategy.getTpPercent());
            strategyInfoDto.setSlPercent(strategy.getSlPercent());
        });

        return list;
    }

    @Override
    public void send(Strategy strategy) {
        senderService.send("", strategy.getChannelId(), generateMessage(strategy));
    }

    private String generateMessage(Strategy strategy) {
        String result = "";

        log.info(STR."generate message for send: strategy.isOpen():\{strategy.isOpen()} strategy.getProfitSum():\{strategy.getProfitSum()}");

        if (!strategy.isOpen() && strategy.getProfitSum() != 0)
            result = generateResultMessage(strategy);

        String instrumentName = strategy.getInstrumentName();

        double bank = resultService.getBank(strategy.getName());
        double sum = statisticsService.getProfitByInstrumentAndStrategy(instrumentName, strategy.getName());
        double percent = statisticsService.getPercentByInstrumentAndStrategy(instrumentName, strategy.getName());

        double percentOfSum = Utilities.roundDouble(sum / bank * 100);

        double sumWithLeverage = instrumentService.getSumWithLeverage(SumType.sum, instrumentName);

        if (!strategy.isOpen())
            return strategy.getMessageForSendClosePosition(result, sumWithLeverage, percent, sum, percentOfSum);

        return strategy.getMessageForSendOpenPosition(sumWithLeverage, percent, sum, percentOfSum, instrumentService, this);
    }

    private String generateResultMessage(Strategy strategy) {
        String result = "";

        double profitWOFee = strategy.getProfitSumWoFee();
        double percentByProfit = getPercentByProfit(strategy);
        double profitReal = getProfitReal(strategy);
        double profitRealPercent = getProfitRealPercent(strategy);

        double bank = resultService.getBank(strategy.getName());
        double dayProfit = resultService.getDayMoney(strategy.getName());

        if (profitWOFee > 0)
            result = STR."PC: \{strategy.getPriceClose()}\n\n✅ \{profitWOFee} \{percentByProfit}% (\{profitReal} \{profitRealPercent}%)\n";
        if (profitWOFee < 0)
            result = STR."PC: \{strategy.getPriceClose()}\n\n❌ \{profitWOFee} \{percentByProfit}% (\{profitReal} \{profitRealPercent}%)\n";
        if (profitWOFee == 0)
            result = STR."PC: \{strategy.getPriceClose()}♻\n";

        int leverage = instrumentService.getLeverageBySymbol(strategy.getInstrumentName());

        result = result
                + getStatString("-", strategy.getLoseMax(), leverage)
                + getStatString("+", strategy.getProfitMax(), leverage)
                + STR."\n\nБанк: \{bank}$ (\{dayProfit}$)";

        return result;
    }

    private String getStatString(String type, double loseProfitMax, int leverage) {
        return STR."\nМакс \{type} : \{loseProfitMax} (\{Utilities.roundDouble(loseProfitMax * leverage)})";
    }

    @Override
    public double getProfitPercent(Strategy strategy) {
        strategy.setProfitPercent(0);

        double currentPrice = instrumentService.getCurrentPrice(strategy.getInstrumentName());
        double priceChange = 0;

        if (strategy.getSide() == Side.BUY)
            priceChange = currentPrice - strategy.getPriceOpen();

        if (strategy.getSide() == Side.SELL)
            priceChange = strategy.getPriceOpen() - currentPrice;

        strategy.setProfitPercent(priceChange / strategy.getPriceOpen() * 100);

        log.info(STR."  getPP_1 | side: \{strategy.getSide()} CP: \{currentPrice} PO: \{strategy.getPriceOpen()} PP: \{Utilities.roundDouble(priceChange)} / \{strategy.getPriceOpen()} * 100 = \{Utilities.roundDouble(strategy.getProfitPercent())}");

        return strategy.getProfitPercent();
    }

    @Override
    public double getProfitPercent(Strategy strategy, double priceOpen) {
        double currentPrice = instrumentService.getCurrentPrice(strategy.getInstrumentName());
        double priceChange = 0;

        if (strategy.getSide() == Side.BUY)
            priceChange = currentPrice - priceOpen;

        if (strategy.getSide() == Side.SELL)
            priceChange = priceOpen - currentPrice;

        double profitPercent = priceChange / strategy.getPriceOpen() * 100;

        log.info(STR."     getPP_2 | side: \{strategy.getSide()} CP: \{currentPrice} PO: \{priceOpen} PP: \{Utilities.roundDouble(priceChange)} / \{strategy.getPriceOpen()} * 100 = \{Utilities.roundDouble(profitPercent)}");

        return profitPercent;
    }

    @Override
    public double getPriceChangePercent(double priceOpen, double price) {
        double profit = (price - priceOpen) / priceOpen * 100;
        log.info(STR."    CDL.PO: \{priceOpen} CP: \{price} PRFT: \{profit}");

        return profit < 0 ? profit * -1 : profit;
    }

    @Override
    public void calcProfitSum(Strategy strategy) {
        double sum = strategy.getAllBetSum();
        if (sum == 0)
            sum = instrumentService.getSumWithLeverage(SumType.sum, strategy.getInstrumentName());

        if (strategy.getSide() == Side.BUY) {
            strategy.setProfitSum((strategy.getPriceClose() - strategy.getPriceOpen()) * (sum / strategy.getPriceOpen()));
            log.info(STR."calc profit BUY: (strategy.getPriceClose():\{strategy.getPriceClose()} - strategy.getPriceOpen():\{strategy.getPriceOpen()}) * sum:\{sum} / strategy.getPriceOpen():\{strategy.getPriceOpen()}");
        }
        if (strategy.getSide() == Side.SELL) {
            strategy.setProfitSum((strategy.getPriceOpen() - strategy.getPriceClose()) * (sum / strategy.getPriceOpen()));
            log.info(STR."calc profit SELL: (strategy.getPriceOpen():\{strategy.getPriceOpen()} - strategy.getPriceClose():\{strategy.getPriceClose()}) * sum:\{sum} / strategy.getPriceOpen():\{strategy.getPriceOpen()}");
        }
        strategy.setAllBetSum(0);

        double fee = instrumentService.getSumOfFee(strategy.getInstrumentName());
        log.info(STR."setProfitWOFee: \{strategy.getProfitSum()} - \{fee}");
        double profitWoFee = Utilities.roundDouble(strategy.getProfitSum() - fee);
        strategy.setProfitSumWoFee(profitWoFee);
    }

    @Override
    public void calcMaxProfitLosePercent(Strategy strategy) {
        double profit = getProfitPercent(strategy);

        if (profit > 0) {
            strategy.setProfitMax(Math.max(profit, strategy.getProfitMax()));
        }
        if (profit < 0) {
            strategy.setLoseMax(Math.min(profit, strategy.getLoseMax()));
        }
    }

    @Override
    public void resetSLTPPercent(Strategy strategy) {
        strategy.setSlPercent(strategyRepository.findByName(strategy.getName()).getSlPercent());
        strategy.setTpPercent(strategyRepository.findByName(strategy.getName()).getTpPercent());
    }

    @Override
    public void resetSide(Strategy strategy) {
        strategy.setSide(null);
    }

    private double getProfitPercentWithLeverageWoFee(Strategy strategy, double leverage) {
        double fee = parameterService.getFee();

        return strategy.getProfitPercent() * leverage - (fee * 2 * 100);
    }

    private double getPercentByProfit(Strategy strategy) {
        log.info(STR."getPercentByProfit: \{strategy.getProfitSumWoFee()} / \{parameterService.getSum() * 100}");

        return Utilities.roundDouble(strategy.getProfitSumWoFee() / parameterService.getSum() * 100);
    }

    private double getProfitReal(Strategy strategy) {
        return Utilities.roundDouble((strategy.getProfitSumWoFee() / (parameterService.getSum() / parameterService.getRealSum())));
    }

    private double getProfitRealPercent(Strategy strategy) {
        return Utilities.roundDouble(getProfitReal(strategy) / parameterService.getRealSum() * 100);
    }
}
