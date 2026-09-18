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
        GamePlugin plugin = getPluginForType(params.gameType());
        Game game = plugin.createGame(params.playerCount(), params.boardSize());
        return gameDao.upsert(game);
    }

    @Override
    public Game getGame(UUID gameId) {
        if (gameId == null) {
            return null;
        }
        return gameDao.findById(gameId.toString()).orElse(null);
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
