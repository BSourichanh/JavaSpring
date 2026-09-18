package com.bsourichanh.javaspring.controller;

import com.bsourichanh.javaspring.dto.GameDescriptionDto;
import com.bsourichanh.javaspring.service.GameCatalog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Locale;

@RestController
@RequestMapping("/api/catalog")
public class GameCatalogController {

    private final GameCatalog gameCatalog;

    public GameCatalogController(GameCatalog gameCatalog) {
        this.gameCatalog = gameCatalog;
    }

    @GetMapping("/games")
    public Collection<GameDescriptionDto> listAvailableGames(Locale locale) {
        return gameCatalog.getAvailableGames(locale);
    }
}
