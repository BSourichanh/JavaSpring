package com.bsourichanh.javaspring.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.taquin.TaquinGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TaquinPlugin implements GamePlugin {

    private final TaquinGameFactory factory;
    private final MessageSource messageSource;
    private final int defaultPlayerCount;
    private final int defaultBoardSize;

    public TaquinPlugin(
            TaquinGameFactory factory,
            MessageSource messageSource,
            @Value("${game.taquin.default-player-count:1}") int defaultPlayerCount,
            @Value("${game.taquin.default-board-size:4}") int defaultBoardSize
    ) {
        this.factory = factory;
        this.messageSource = messageSource;
        this.defaultPlayerCount = defaultPlayerCount;
        this.defaultBoardSize = defaultBoardSize;
    }

    @Override
    public String getFactoryId() {
        return factory.getGameFactoryId(); // "15 puzzle"
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.taquin.name", null, locale);
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

    @Override
    public Game createGame(Integer playerCount, Integer boardSize, java.util.Collection<java.util.UUID> playerIds) {
        int size = (boardSize != null && boardSize > 0) ? boardSize : defaultBoardSize;
        if (playerIds != null && !playerIds.isEmpty()) {
            return factory.createGame(size, java.util.Set.of(playerIds.iterator().next()));
        }
        return createGame(playerCount, boardSize);
    }
}

