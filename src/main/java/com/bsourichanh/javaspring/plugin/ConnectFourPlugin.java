package com.bsourichanh.javaspring.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.connectfour.ConnectFourGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ConnectFourPlugin implements GamePlugin {

    private final ConnectFourGameFactory factory;
    private final MessageSource messageSource;
    private final int defaultPlayerCount;
    private final int defaultBoardSize;

    public ConnectFourPlugin(
            ConnectFourGameFactory factory,
            MessageSource messageSource,
            @Value("${game.connectfour.default-player-count:2}") int defaultPlayerCount,
            @Value("${game.connectfour.default-board-size:7}") int defaultBoardSize
    ) {
        this.factory = factory;
        this.messageSource = messageSource;
        this.defaultPlayerCount = defaultPlayerCount;
        this.defaultBoardSize = defaultBoardSize;
    }

    @Override
    public String getFactoryId() {
        return factory.getGameFactoryId(); // "connect4"
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.connectfour.name", null, locale);
    }

    @Override
    public int getDefaultPlayerCount() {
        return defaultPlayerCount;
    }

    @Override
    public int getDefaultBoardSize() {
        return defaultBoardSize;
    }

    @Override
    public Game createGame(Integer playerCount, Integer boardSize) {
        int players = (playerCount != null && playerCount > 0) ? playerCount : defaultPlayerCount;
        int size = (boardSize != null && boardSize > 0) ? boardSize : defaultBoardSize;
        return factory.createGame(players, size);
    }
}
