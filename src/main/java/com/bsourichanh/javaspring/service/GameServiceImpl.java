package com.bsourichanh.javaspring.service;

import com.bsourichanh.javaspring.dao.GameDao;
import com.bsourichanh.javaspring.dto.GameCreationParams;
import com.bsourichanh.javaspring.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
public class GameServiceImpl implements GameService {

    private final Collection<GamePlugin> gamePlugins;
    private final GameDao gameDao;

    public GameServiceImpl(Collection<GamePlugin> gamePlugins, GameDao gameDao) {
        this.gamePlugins = gamePlugins;
        this.gameDao = gameDao;
    }

    @Override
    public Game createGame(GameCreationParams params) {
        return createGame(params, null);
    }

    @Override
    public Game createGame(GameCreationParams params, String userId) {
        GamePlugin plugin = getPluginForType(params.gameType());
        java.util.UUID userUuid = null;
        if (userId != null && !userId.isBlank()) {
            try {
                userUuid = java.util.UUID.fromString(userId);
            } catch (IllegalArgumentException ignored) {}
        }
        Game game;
        if (userUuid != null) {
            game = plugin.createGame(params.playerCount(), params.boardSize(), java.util.List.of(userUuid));
        } else {
            game = plugin.createGame(params.playerCount(), params.boardSize());
        }
        return gameDao.upsert(game);
    }

    @Override
    public Game getGame(UUID gameId) {
        if (gameId == null) {
            return null;
        }
        return gameDao.findById(gameId.toString()).orElse(null);
    }

    @Override
    public Collection<Game> getGamesForUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return java.util.List.of();
        }
        return gameDao.findAll()
                .filter(game -> game.getPlayerIds().stream()
                        .map(Object::toString)
                        .anyMatch(id -> id.equals(userId)))
                .toList();
    }

    @Override
    public Game playMove(UUID gameId, String userId, int x, int y) {
        Game game = getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Partie introuvable : " + gameId);
        }
        if (game.getCurrentPlayerId() == null || !game.getCurrentPlayerId().toString().equals(userId)) {
            throw new IllegalStateException("Ce n'est pas le tour du joueur : " + userId);
        }

        fr.le_campus_numerique.square_games.engine.CellPosition target =
                new fr.le_campus_numerique.square_games.engine.CellPosition(x, y);

        // Recherche d'un jeton déplaçable appartenant au joueur
        fr.le_campus_numerique.square_games.engine.Token tokenToMove = game.getRemainingTokens().stream()
                .filter(t -> t.getOwnerId().map(id -> id.toString().equals(userId)).orElse(false))
                .filter(t -> t.getAllowedMoves().contains(target))
                .findFirst()
                .orElse(null);

        if (tokenToMove == null) {
            tokenToMove = game.getBoard().values().stream()
                    .filter(t -> t.getOwnerId().map(id -> id.toString().equals(userId)).orElse(true))
                    .filter(t -> t.getAllowedMoves().contains(target))
                    .findFirst()
                    .orElse(null);
        }

        if (tokenToMove == null) {
            throw new IllegalArgumentException("Aucun coup valide possible vers la case (" + x + ", " + y + ")");
        }

        try {
            tokenToMove.moveTo(target);
        } catch (fr.le_campus_numerique.square_games.engine.InvalidPositionException e) {
            throw new IllegalArgumentException("Position invalide : " + e.getMessage(), e);
        }

        return gameDao.upsert(game);
    }


    private GamePlugin getPluginForType(String type) {
        if (type == null || type.isBlank()) {
            type = "tictactoe";
        }
        String normalizedType = type.toLowerCase().trim();
        for (GamePlugin gamePlugin : gamePlugins) {
            String factoryId = gamePlugin.getFactoryId().toLowerCase();
            if (factoryId.equals(normalizedType)
                    || (normalizedType.contains("tic") && factoryId.contains("tictac"))
                    || (normalizedType.contains("taquin") && factoryId.contains("puzzle"))
                    || (normalizedType.contains("connect") && factoryId.contains("connect"))) {
                return gamePlugin;
            }
        }
        throw new IllegalArgumentException("Type de jeu non supporté : " + type);
    }
}
