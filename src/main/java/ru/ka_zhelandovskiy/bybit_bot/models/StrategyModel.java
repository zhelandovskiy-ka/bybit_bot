package ru.ka_zhelandovskiy.bybit_bot.models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "strategies")
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
    private boolean active;
    private boolean allowOrder;
}
