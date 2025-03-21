package ru.ka_zhelandovskiy.bybit_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ka_zhelandovskiy.bybit_bot.models.BankHistoryModel;

@Repository
public interface BankHistoryRepository extends JpaRepository<BankHistoryModel, String> {
}
