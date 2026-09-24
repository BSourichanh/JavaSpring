package com.bsourichanh.javaspring.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import java.util.Locale;

public interface GamePlugin {
    String getFactoryId();
    String getName(Locale locale);
    int getDefaultPlayerCount();
    int getDefaultBoardSize();
    Game createGame(Integer playerCount, Integer boardSize);

    default Game createGame(Integer playerCount, Integer boardSize, java.util.Collection<java.util.UUID> playerIds) {
        return createGame(playerCount, boardSize);
    }
}
