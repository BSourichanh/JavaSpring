package com.bsourichanh.javaspring.plugin;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.GameStatus;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FixedTaquinGameTest {

    @Test
    void testInitialStateAndNeighbors() {
        UUID playerId = UUID.randomUUID();
        // Grille résolue 3x3 :
        // 1 2 3
        // 4 5 6
        // 7 8 _
        List<Integer> tiles = List.of(1, 2, 3, 4, 5, 6, 7, 8, 0);
        FixedTaquinGame game = new FixedTaquinGame(UUID.randomUUID(), playerId, 3, tiles);

        assertEquals(GameStatus.TERMINATED, game.getStatus(), "La grille résolue doit être TERMINATED");

        // Voisins de la case vide (2, 2) :
        // (1, 2) [tuile 8 à gauche]
        // (2, 1) [tuile 6 au-dessus]
        Map<CellPosition, Token> board = game.getBoard();
        Token token8 = board.get(new CellPosition(1, 2));
        Token token6 = board.get(new CellPosition(2, 1));
        Token token5 = board.get(new CellPosition(1, 1)); // Diagonale

        assertNotNull(token8);
        assertNotNull(token6);
        assertNotNull(token5);

        assertEquals(Set.of(new CellPosition(2, 2)), token8.getAllowedMoves(), "La tuile 8 doit pouvoir glisser en (2,2)");
        assertEquals(Set.of(new CellPosition(2, 2)), token6.getAllowedMoves(), "La tuile 6 doit pouvoir glisser en (2,2)");
        assertTrue(token5.getAllowedMoves().isEmpty(), "La tuile en diagonale ne doit PAS pouvoir glisser");
    }

    @Test
    void testMoveTileAndStatusChange() throws InvalidPositionException {
        UUID playerId = UUID.randomUUID();
        // Grille résolue :
        List<Integer> tiles = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 0));
        FixedTaquinGame game = new FixedTaquinGame(UUID.randomUUID(), playerId, 3, tiles);

        // Déplacer la tuile 8 de (1, 2) vers (2, 2)
        Token token8 = game.getBoard().get(new CellPosition(1, 2));
        token8.moveTo(new CellPosition(2, 2));

        // Maintenant (1, 2) est vide, et (2, 2) contient la tuile 8
        assertNull(game.getBoard().get(new CellPosition(1, 2)));
        assertEquals("8", game.getBoard().get(new CellPosition(2, 2)).getName());
        assertEquals(GameStatus.ONGOING, game.getStatus(), "Le jeu n'est plus résolu");

        // Remettre la tuile 8 à sa place
        Token token8Moved = game.getBoard().get(new CellPosition(2, 2));
        token8Moved.moveTo(new CellPosition(1, 2));

        assertEquals(GameStatus.TERMINATED, game.getStatus(), "Le jeu redevient TERMINATED");
    }
}
