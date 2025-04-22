package ru.ka_zhelandovskiy.bybit_bot.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ka_zhelandovskiy.bybit_bot.dto.Order;
import ru.ka_zhelandovskiy.bybit_bot.dto.OrderResponse;
import ru.ka_zhelandovskiy.bybit_bot.services.BybitService;
import ru.ka_zhelandovskiy.bybit_bot.services.OrderService;
import ru.ka_zhelandovskiy.bybit_bot.services.ParameterService;
import ru.ka_zhelandovskiy.bybit_bot.services.StrategyService;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final BybitService bybitService;
    private final StrategyService strategyService;
    private final ParameterService parameterService;

//    private final List<String> orderIdList = new ArrayList<>();

    @Override
    @Scheduled(fixedRate = 3000L)
    public void updateOrdersPrice() {

        if (!parameterService.isTestMode()) {
            boolean isOpenedStrategy = strategyService.isOpenedStrategy();

            log.info("OPENED STRATEGY IS {}", isOpenedStrategy);

            if (isOpenedStrategy) {
                log.info("CHECK OPEN ORDERS");
                for (Order order : bybitService.getOpenOrder()) {
                    String price = bybitService.getPriceFromOrderBook(order.getSymbol(), order.getSide());
                    if (!order.getPrice().equals(price)) {
                        log.info("ORDER EXIST TRY MODIFY, id: {}, newPrice:{}", order.getOrderId(), price);
                        OrderResponse orderResponse = bybitService.modifyLimitOrder(order, price);
                        log.info("ORDER MODIFIED {}", orderResponse);
                    }
                }
                log.info("ORDERS CHECKED");
            }
        }

/*        log.info("CHECK OPEN ORDERS current orderIds size: {}", orderIdList.size());
        for (Order order : bybitService.getOpenOrder()) {
            if (orderIdList.contains(order.getOrderId())) {
                String price = bybitService.getPriceFromOrderBook(order.getSymbol(), order.getSide());
                if (!order.getPrice().equals(price)) {
                    log.info("ORDER EXIST TRY MODIFY, id: {}, newPrice:{}", order.getOrderId(), price);
                    OrderResponse orderResponse = bybitService.modifyLimitOrder(order, price);
                    log.info("ORDER MODIFIED {}", orderResponse);
                }
            } else {
                log.info("ORDER NOT EXIST, DELETE");
                orderIdList.remove(order.getOrderId());
            }
        }
        log.info("ORDERS CHECKED current orderIds size: {}", orderIdList.size());*/
    }

    @Override
    public void addOrderIdToList(String id) {
//        log.info("ADD orderId TO LIST {}", id);
//        orderIdList.add(id);
    }
}
