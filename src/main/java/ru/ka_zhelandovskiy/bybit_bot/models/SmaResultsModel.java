package ru.ka_zhelandovskiy.bybit_bot.models;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sma_results")
public class SmaResultsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String instrument;
    private int smaMin;
    private int smaMax;
    private double profit;
    private double avgProfit;
    private double avgPositiveProfit;
    private double avgNegativeProfit;
    private int positiveCount;
    private int negativeCount;
    private double patency;
    private int timeFrame;
}
