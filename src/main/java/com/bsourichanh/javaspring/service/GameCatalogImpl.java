package com.bsourichanh.javaspring.service;

import com.bsourichanh.javaspring.dto.GameDescriptionDto;
import com.bsourichanh.javaspring.plugin.GamePlugin;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Locale;

@Service
public class GameCatalogImpl implements GameCatalog {

    private final Collection<GamePlugin> plugins;

    public GameCatalogImpl(Collection<GamePlugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public Collection<GameDescriptionDto> getAvailableGames(Locale locale) {
        return plugins.stream()
                .map(plugin -> new GameDescriptionDto(
                        plugin.getFactoryId(),
                        plugin.getName(locale),
                        plugin.getDefaultPlayerCount(),
                        plugin.getDefaultBoardSize()
                ))
                .toList();
    }
}
