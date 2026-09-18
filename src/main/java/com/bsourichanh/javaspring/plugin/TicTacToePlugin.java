package com.bsourichanh.javaspring.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import java.util.Locale;

@Component
public class TicTacToePlugin implements GamePlugin {

    private final TicTacToeGameFactory factory;
    private final MessageSource messageSource;
    private final int defaultPlayerCount;
    private final int defaultBoardSize;

    public TicTacToePlugin(
            TicTacToeGameFactory factory,
            MessageSource messageSource,
            @Value("${game.tictactoe.default-player-count:2}") int defaultPlayerCount,
            @Value("${game.tictactoe.default-board-size:3}") int defaultBoardSize
    ) {
        this.factory = factory;
        this.messageSource = messageSource;
        this.defaultPlayerCount = defaultPlayerCount;
        this.defaultBoardSize = defaultBoardSize;
    }

    @Override
    public String getFactoryId() { return factory.getGameFactoryId(); }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.tictactoe.name", null, locale);
    }

    @Override
    public int getDefaultPlayerCount() { return defaultPlayerCount; }

    @Override
    public int getDefaultBoardSize() { return defaultBoardSize; }

    @Override
    public Game createGame(Integer playerCount, Integer boardSize) {
        int players = (playerCount != null && playerCount > 0) ? playerCount : defaultPlayerCount;
        int size = (boardSize != null && boardSize > 0) ? boardSize : defaultBoardSize;
        return factory.createGame(players, size);
    }
}