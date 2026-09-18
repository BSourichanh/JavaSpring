package com.bsourichanh.javaspring.service;

import com.bsourichanh.javaspring.dto.GameCreationParams;
import fr.le_campus_numerique.square_games.engine.Game;

import java.util.UUID;

public interface GameService {
    Game createGame(GameCreationParams params);
    Game getGame(UUID gameId);
}
