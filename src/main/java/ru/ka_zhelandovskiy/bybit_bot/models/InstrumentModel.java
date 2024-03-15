package ru.ka_zhelandovskiy.bybit_bot.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.ToString;

@Getter
@Entity
@ToString
@Table(name = "instruments")
public class InstrumentModel {
    @Id
    private String symbol;
    private int leverage;
    private int qp;
    private boolean ignore;
    private boolean reverse;
    private double maxChange;
}
