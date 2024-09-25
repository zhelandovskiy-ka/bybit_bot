package ru.ka_zhelandovskiy.bybit_bot.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ka_zhelandovskiy.bybit_bot.models.StatisticsModel;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<StatisticsModel, Integer> {
    List<StatisticsModel> findByStrategy(String instrument);

    List<StatisticsModel> findByInstrumentAndStrategy(String instrument, String strategy);

    List<StatisticsModel> findByInstrumentAndStrategyAndResult(String instrument, String strategy, int result);

    List<StatisticsModel> findByInstrumentAndStrategyAndProfitIsNotNull(String instrument, String strategy);

    List<StatisticsModel> findByInstrumentAndProfitIsNotNull(String instrument);

    List<StatisticsModel> findByStrategyAndDateBetween(String strategy, LocalDateTime date1, LocalDateTime date2);
}
