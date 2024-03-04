package ru.ka_zhelandovskiy.bybit_bot.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString
@Table(name = "results")
public class ResultsModel {
    @Id
    private String name;
    private int dayMinus;
    private int allMinus;
    private int dayPlus;
    private int allPlus;
    private double allMinusMoney;
    private double allPlusMoney;
    private double dayMoney;
    private double avgLose;
    private double avgWin;
    private double bank;
    private double startBank;
    private double maxProfit;
    private double maxLose;
}
