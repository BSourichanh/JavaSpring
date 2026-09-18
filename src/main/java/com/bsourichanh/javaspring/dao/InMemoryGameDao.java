package com.bsourichanh.javaspring.dao;

import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Repository
@Profile({"memory", "default"})
public class InMemoryGameDao implements GameDao {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    @Override
    public Stream<Game> findAll() {
        return games.values().stream();
    }

    @Override
    public Optional<Game> findById(String gameId) {
        if (gameId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(games.get(gameId));
    }

    @Override
    public Game upsert(Game game) {
        if (game != null && game.getId() != null) {
            games.put(game.getId().toString(), game);
        }
        return game;
    }

    @Override
    public void delete(String gameId) {
        if (gameId != null) {
            games.remove(gameId);
        }
    }
}
