package ru.ka_zhelandovskiy.bybit_bot.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@Entity
@ToString
@Table(name = "statistics")
public class StatisticsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int number;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    private String strategy;
    private String side;
    private String instrument;
    private double open;
    private double close;
    private double profit;
    private int result;
    private double maxProfit;
    private double maxLose;

    @PrePersist
    protected void onCreate() {
        date = new Date();
    }
}
