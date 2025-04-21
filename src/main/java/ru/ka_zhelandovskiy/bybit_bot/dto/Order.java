package ru.ka_zhelandovskiy.bybit_bot.dto;

import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.trade.Side;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private String symbol;
    private String orderType;
    private String orderId;
    private String orderStatus;
    private String price;
    private String createdTime;
    private String leavesValue;
    private String updatedTime;
    private String side;
    private String qty;

    public Side getSide() {
        return Side.valueOf(side.toUpperCase());
    }

    public TradeOrderType getOrderType() {
        return TradeOrderType.valueOf(orderType.toUpperCase());
    }
}
