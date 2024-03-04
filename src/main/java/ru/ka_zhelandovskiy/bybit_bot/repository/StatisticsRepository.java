package ru.ka_zhelandovskiy.bybit_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ka_zhelandovskiy.bybit_bot.models.StatisticsModel;

import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<StatisticsModel, Integer> {
    List<StatisticsModel> findByInstrumentAndStrategy(String instrument, String strategy);
    List<StatisticsModel> findByInstrumentAndStrategyAndResult(String instrument, String strategy, int result);
    List<StatisticsModel> findByInstrumentAndStrategyAndProfitIsNotNull(String instrument, String strategy);
}
