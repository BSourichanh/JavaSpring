package com.bsourichanh.javaspring.dao;

import com.bsourichanh.javaspring.entity.GameEntity;
import com.bsourichanh.javaspring.entity.GameTokenEntity;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.Token;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Repository
@Profile("jpa")
public class JpaGameDao implements GameDao {

    private final GameEntityRepository repository;
    private final Map<String, Game> gameCache = new ConcurrentHashMap<>();

    public JpaGameDao(GameEntityRepository repository) {
        this.repository = repository;
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
        return repository.findById(gameId).map(entity -> gameCache.get(entity.getId()));
    }

    @Override
    public Game upsert(Game game) {
        if (game == null || game.getId() == null) {
            return game;
        }
        gameCache.put(game.getId().toString(), game);
        GameEntity entity = toEntity(game);
        repository.save(entity);
        return game;
    }

    @Override
    public void delete(String gameId) {
        if (gameId == null) {
            return;
        }
        gameCache.remove(gameId);
        repository.deleteById(gameId);
    }

    private GameEntity toEntity(Game game) {
        GameEntity entity = new GameEntity();
        entity.setId(game.getId().toString());
        entity.setFactoryId(game.getFactoryId());
        entity.setBoardSize(game.getBoardSize());
        entity.setStatus(game.getStatus().name());
        entity.setCurrentPlayerId(game.getCurrentPlayerId() != null ? game.getCurrentPlayerId().toString() : null);
        entity.setPlayerIds(String.join(",", game.getPlayerIds().stream().map(Object::toString).toList()));

        // Sauvegarde des jetons sur le plateau
        for (Map.Entry<CellPosition, Token> entry : game.getBoard().entrySet()) {
            Token token = entry.getValue();
            CellPosition pos = entry.getKey();
            String ownerId = token.getOwnerId().map(Object::toString).orElse(null);
            entity.getTokens().add(new GameTokenEntity(ownerId, token.getName(), false, pos.x(), pos.y()));
        }

        // Sauvegarde des jetons restants
        for (Token token : game.getRemainingTokens()) {
            String ownerId = token.getOwnerId().map(Object::toString).orElse(null);
            entity.getTokens().add(new GameTokenEntity(ownerId, token.getName(), false, null, null));
        }

        // Sauvegarde des jetons éliminés
        for (Token token : game.getRemovedTokens()) {
            String ownerId = token.getOwnerId().map(Object::toString).orElse(null);
            entity.getTokens().add(new GameTokenEntity(ownerId, token.getName(), true, null, null));
        }

        return entity;
    }
}
