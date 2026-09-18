package com.bsourichanh.javaspring.dao;

import fr.le_campus_numerique.square_games.engine.Game;

import java.util.Optional;
import java.util.stream.Stream;

public interface GameDao {

    /**
     * Retourne toutes les parties sous forme de Stream.
     */
    Stream<Game> findAll();

    /**
     * Recherche une partie par son identifiant UUID (sous forme de chaîne).
     */
    Optional<Game> findById(String gameId);

    /**
     * Insère ou met à jour une partie.
     */
    Game upsert(Game game);

    /**
     * Supprime une partie par son identifiant.
     */
    void delete(String gameId);
}
