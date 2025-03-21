package ru.ka_zhelandovskiy.bybit_bot.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bank_history")
public class BankHistoryModel {
    @Id
    private String time;
    private double so120;
    private double sor120;
    private double sorev120;
    private double realBank;
}
