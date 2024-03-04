package ru.ka_zhelandovskiy.bybit_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ka_zhelandovskiy.bybit_bot.models.StrategyModel;

import java.util.List;

public interface StrategyRepository extends JpaRepository<StrategyModel, String> {
    List<StrategyModel> findAllByName(String name);

    List<StrategyModel> findAllByTimeFrame(int timeFrame);

    StrategyModel findByName(String name);
}
