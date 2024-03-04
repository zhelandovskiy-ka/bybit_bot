package ru.ka_zhelandovskiy.bybit_bot.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "strategies")
@ToString
public class StrategyModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String channelId;
    private int timeFrame;
    @Column(columnDefinition = "jsonb")
    private String parameters;
    private String type;
    private double tpPercent;
    private double slPercent;
}
