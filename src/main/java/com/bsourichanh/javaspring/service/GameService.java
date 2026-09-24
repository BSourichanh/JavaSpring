package com.bsourichanh.javaspring.service;

import com.bsourichanh.javaspring.dto.GameCreationParams;
import fr.le_campus_numerique.square_games.engine.Game;

import java.util.Collection;
import java.util.UUID;

public interface GameService {
    Game createGame(GameCreationParams params);
    Game createGame(GameCreationParams params, String userId);
    Game getGame(UUID gameId);
    Collection<Game> getGamesForUser(String userId);
    Game playMove(UUID gameId, String userId, int x, int y);
}

