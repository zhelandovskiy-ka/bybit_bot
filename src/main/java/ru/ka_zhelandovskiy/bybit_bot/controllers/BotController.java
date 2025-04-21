package ru.ka_zhelandovskiy.bybit_bot.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ka_zhelandovskiy.bybit_bot.dto.OrderBookResponse;
import ru.ka_zhelandovskiy.bybit_bot.dto.OrderResponse;
import ru.ka_zhelandovskiy.bybit_bot.services.BybitService;
import ru.ka_zhelandovskiy.bybit_bot.services.ResultService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bot")
public class BotController {

    private final ResultService resultService;
    private final BybitService bybitService;

    @GetMapping("/banks")
    public String getCurrentBalances() {
        resultService.getAllResult().forEach(
                result -> System.out.println(STR."\{result.getName()}: \{result.getBank()}"));
        return "ok";
    }

    @GetMapping("/ob")
    public OrderBookResponse getOrderBook() {
        return bybitService.getOrderBook("BTCUSDT", 10);
    }

    @PostMapping("/limit-order-test")
    public OrderResponse placeLimitOrder() {
//        String price = bybitService.getPriceFromOrderBook("ANIMEUSDT", Side.BUY);

//        OrderResponse order1 = bybitService.placeLimitOrder("ANIMEUSDT", "1734", Side.BUY, "0.015");
//        OrderResponse order2 = bybitService.placeLimitOrder("ANIMEUSDT", "1734", Side.BUY, "0.01501");
//        OrderResponse order3 = bybitService.placeLimitOrder("ANIMEUSDT", "1734", Side.BUY, "0.01505");

        System.out.println(bybitService.getOpenOrder().toString());

/*        System.out.println("order = " + order);

        Order openOrder = bybitService.getOpenOrderById(order.getResult().getOrderId());

        System.out.println("openOrder = " + openOrder);

        Utilities.sleep(3);

        OrderResponse modifiedOrder = bybitService.modifyLimitOrder(openOrder, "0.01693");

        System.out.println("modifiedOrder = " + modifiedOrder);*/

        return null;
//        return bybitService.placeLimitOrder("ANIMEUSDT", "1734", Side.BUY, "0.01714");
    }
}
