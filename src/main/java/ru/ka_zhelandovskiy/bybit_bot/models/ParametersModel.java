package ru.ka_zhelandovskiy.bybit_bot.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "parameters")
public class ParametersModel {
    @Id
    private String parameter;
    private String value;
}
