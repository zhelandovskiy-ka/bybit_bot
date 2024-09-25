package ru.ka_zhelandovskiy.bybit_bot.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Date;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "statistics")
public class StatisticsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int number;
    @CreationTimestamp
    private LocalDateTime date;
    private String strategy;
    private String side;
    private String instrument;
    private double open;
    private double close;
    private double profit;
    private int result;
    private double maxProfit;
    private double maxLose;
}
