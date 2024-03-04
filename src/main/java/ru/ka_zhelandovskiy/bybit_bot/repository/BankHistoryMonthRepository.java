package ru.ka_zhelandovskiy.bybit_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ka_zhelandovskiy.bybit_bot.models.BankHistoryMonthModel;


public interface BankHistoryMonthRepository extends JpaRepository<BankHistoryMonthModel, String> {
}
