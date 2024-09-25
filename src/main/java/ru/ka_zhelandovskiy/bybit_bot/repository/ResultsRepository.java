package ru.ka_zhelandovskiy.bybit_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ka_zhelandovskiy.bybit_bot.models.ResultsModel;

import java.util.Optional;

@Repository
public interface ResultsRepository extends JpaRepository<ResultsModel, String> {
    ResultsModel findByName(String name);
}
