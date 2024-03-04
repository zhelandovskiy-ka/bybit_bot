package ru.ka_zhelandovskiy.bybit_bot.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.ToString;

@Entity
@Getter
@ToString
@Table(name = "bank_history_month")
public class BankHistoryMonthModel {
    @Id
    private String day;
    private double so120;
    private double sor120;
    private double sorev120;
    private double realBank;
}
