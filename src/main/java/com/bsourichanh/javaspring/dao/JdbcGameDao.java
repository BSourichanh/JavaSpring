package com.bsourichanh.javaspring.dao;

import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Repository
@Profile("jdbc")
public class JdbcGameDao implements GameDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Map<String, Game> gameCache = new ConcurrentHashMap<>();

    public JdbcGameDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Stream<Game> findAll() {
        return gameCache.values().stream();
    }

    @Override
    public Optional<Game> findById(String gameId) {
        if (gameId == null) {
            return Optional.empty();
        }
        Game game = gameCache.get(gameId);
        if (game != null) {
            return Optional.of(game);
        }
        String sql = "SELECT COUNT(*) FROM games WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", gameId);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        if (count != null && count > 0) {
            return Optional.ofNullable(gameCache.get(gameId));
        }
        return Optional.empty();
    }

    @Override
    public Game upsert(Game game) {
        if (game == null || game.getId() == null) {
            return game;
        }
        gameCache.put(game.getId().toString(), game);

        String sql = """
            INSERT INTO games (id, factory_id, board_size, status, current_player_id, player_ids)
            VALUES (:id, :factoryId, :boardSize, :status, :currentPlayerId, :playerIds)
            ON DUPLICATE KEY UPDATE
                factory_id = VALUES(factory_id),
                board_size = VALUES(board_size),
                status = VALUES(status),
                current_player_id = VALUES(current_player_id),
                player_ids = VALUES(player_ids)
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", game.getId().toString())
                .addValue("factoryId", game.getFactoryId())
                .addValue("boardSize", game.getBoardSize())
                .addValue("status", game.getStatus().name())
                .addValue("currentPlayerId", game.getCurrentPlayerId() != null ? game.getCurrentPlayerId().toString() : null)
                .addValue("playerIds", String.join(",", game.getPlayerIds().stream().map(Object::toString).toList()));

        jdbcTemplate.update(sql, params);
        return game;
    }

    @Override
    public void delete(String gameId) {
        if (gameId == null) {
            return;
        }
        gameCache.remove(gameId);
        String sql = "DELETE FROM games WHERE id = :id";
        jdbcTemplate.update(sql, new MapSqlParameterSource("id", gameId));
    }
}