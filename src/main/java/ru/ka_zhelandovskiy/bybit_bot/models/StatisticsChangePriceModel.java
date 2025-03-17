package ru.ka_zhelandovskiy.bybit_bot.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "change_price_stat")
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsChangePriceModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument")
    private String instrument;

    @Column(name = "timeframe")
    private String timeFrame;

    @Column(name = "ok_percent")
    private double okPercent;

    @Column(name = "ok_final_percent")
    private double okFinalPercent;

    @Column(name = "avg_diff")
    private double avgDifferent;

    @Column(name = "avg_candle_size")
    private double avgCandleSize;

    @Column(name = "side")
    private String side;

    @Column(name = "volume")
    private long volume;

    @Column(name = "candle_count")
    private long candleCount;
}
