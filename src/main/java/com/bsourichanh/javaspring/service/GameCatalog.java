package com.bsourichanh.javaspring.service;

import com.bsourichanh.javaspring.dto.GameDescriptionDto;
import java.util.Collection;
import java.util.Locale;

public interface GameCatalog {
    Collection<GameDescriptionDto> getAvailableGames(Locale locale);
}
