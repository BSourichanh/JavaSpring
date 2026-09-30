package com.bsourichanh.javaspring.service;

import com.bsourichanh.javaspring.dao.GameDao;
import com.bsourichanh.javaspring.dto.GameCreationParams;
import com.bsourichanh.javaspring.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
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
        UUID userUuid = null;
        if (userId != null && !userId.isBlank()) {
            try {
                userUuid = UUID.fromString(userId);
            } catch (IllegalArgumentException ignored) {}
        }
        Game game;
        if (userUuid != null) {
            game = plugin.createGame(params.playerCount(), params.boardSize(), List.of(userUuid));
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
            return List.of();
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

        final CellPosition clickedPosition = new CellPosition(x, y);
        final CellPosition colGravityTarget = new CellPosition(x, -1);

        Token tokenToMove = null;
        CellPosition targetToApply = clickedPosition;

        // 1. Recherche dans les jetons restants (Morpion avec (x,y), ou Puissance 4 avec colonne (x,-1))
        tokenToMove = game.getRemainingTokens().stream()
                .filter(t -> t.getOwnerId().map(id -> id.toString().equals(userId)).orElse(false))
                .filter(t -> t.getAllowedMoves().contains(clickedPosition) || t.getAllowedMoves().contains(colGravityTarget))
                .findFirst()
                .orElse(null);

        if (tokenToMove != null) {
            if (!tokenToMove.getAllowedMoves().contains(clickedPosition) && tokenToMove.getAllowedMoves().contains(colGravityTarget)) {
                targetToApply = colGravityTarget;
            }
        }

        // 2. Recherche sur le plateau (Taquin)
        if (tokenToMove == null) {
            // Cas 2a : Le joueur a cliqué sur la tuile à déplacer vers l'espace vide voisin
            Token clickedToken = game.getBoard().get(clickedPosition);
            if (clickedToken != null && !clickedToken.getAllowedMoves().isEmpty()
                    && clickedToken.getOwnerId().map(id -> id.toString().equals(userId)).orElse(true)) {
                tokenToMove = clickedToken;
                targetToApply = clickedToken.getAllowedMoves().iterator().next();
            }
        }

        if (tokenToMove == null) {
            // Cas 2b : Le joueur a cliqué sur la case vide vers laquelle glisser une tuile voisine
            tokenToMove = game.getBoard().values().stream()
                    .filter(t -> t.getOwnerId().map(id -> id.toString().equals(userId)).orElse(true))
                    .filter(t -> t.getAllowedMoves().contains(clickedPosition))
                    .findFirst()
                    .orElse(null);
        }

        if (tokenToMove == null) {
            throw new IllegalArgumentException("Aucun coup valide possible vers la case (" + x + ", " + y + ")");
        }

        try {
            tokenToMove.moveTo(targetToApply);
        } catch (InvalidPositionException e) {
            throw new IllegalArgumentException("Position invalide : " + e.getMessage(), e);
        }

        return gameDao.upsert(game);
    }

    /**
     * Sélectionne le plugin de jeu adéquat via le pattern Strategy (Open-Closed Principle).
     */
    private GamePlugin getPluginForType(String type) {
        String targetType = (type == null || type.isBlank()) ? "tictactoe" : type;
        return gamePlugins.stream()
                .filter(plugin -> plugin.canHandle(targetType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Type de jeu non supporté : " + type));
    }
}
