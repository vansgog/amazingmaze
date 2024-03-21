package com.example.amazingmaze.repositories;

import com.example.amazingmaze.utils.GameResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class GameRepository {
    private static final String URL = "jdbc:postgresql://localhost:5432/skillbox";
    private static final String USER = "skillbox";
    private static final String PASSWORD = "skillbox";

    public void saveGameResult(String username, int size, int complexity, long duration, int score) {
        String sql = "INSERT INTO GameResults (username, size, complexity, duration, score) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setInt(2, size);
            preparedStatement.setInt(3, complexity);
            preparedStatement.setLong(4, duration);
            preparedStatement.setInt(5, score);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public List<GameResult> findAllResults() {
        List<GameResult> results = new ArrayList<>();
        String sql = "SELECT * FROM GameResults ORDER BY score DESC";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                results.add(new GameResult(
                        resultSet.getString("username"),
                        resultSet.getInt("size"),
                        resultSet.getInt("complexity"),
                        resultSet.getLong("duration"),
                        resultSet.getInt("score")
                ));
            }
        } catch (SQLException e) {
            log.error("Ошибка при извлечении результатов игры", e);
        }
        return results;
    }

    public void displayResults() {
        log.info("Лучшие результаты:");
        String sql = "SELECT username, size, complexity, duration, score FROM GameResults ORDER BY score DESC LIMIT 5";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                log.info("Игрок: " + resultSet.getString("username") +
                        ", Размер лабиринта: " + resultSet.getInt("size") +
                        ", Сложность: " + resultSet.getInt("complexity") +
                        ", Время прохождения: " + resultSet.getLong("duration") + "сек" +
                        ", Очки: " + resultSet.getInt("score"));
            }
        } catch (SQLException e) {
            log.info(e.getMessage());
        }
    }
}