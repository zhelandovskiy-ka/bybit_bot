package ru.ka_zhelandovskiy.bybit_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ka_zhelandovskiy.bybit_bot.models.BankHistoryModel;

public interface BankHistoryRepository extends JpaRepository<BankHistoryModel, String> {
}
