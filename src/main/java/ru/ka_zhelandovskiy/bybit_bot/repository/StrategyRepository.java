package ru.ka_zhelandovskiy.bybit_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ka_zhelandovskiy.bybit_bot.models.StrategyModel;

import java.util.List;

@Repository
public interface StrategyRepository extends JpaRepository<StrategyModel, String> {
    List<StrategyModel> findAllByName(String name);

    List<StrategyModel> findAllByTimeFrame(int timeFrame);

    List<StrategyModel> findAllByTimeFrameAndActiveTrue(int timeFrame);

    List<StrategyModel> findByActiveTrue();

    StrategyModel findByName(String name);
}
