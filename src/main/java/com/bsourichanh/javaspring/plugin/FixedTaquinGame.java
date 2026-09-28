package com.bsourichanh.javaspring.plugin;

import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameStatus;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FixedTaquinGame implements Game {

    public static final String FACTORY_ID = "15 puzzle";

    private final UUID id;
    private final UUID playerId;
    private final int boardSize;
    private final List<Tile> tiles;

    public FixedTaquinGame(int boardSize, UUID playerId) {
        if (boardSize < 3 || boardSize > 8) {
            throw new IllegalArgumentException("boardSize must be between 3 and 8");
        }
        this.id = UUID.randomUUID();
        this.playerId = Objects.requireNonNull(playerId, "playerId must not be null");
        this.boardSize = boardSize;
        int totalCells = boardSize * boardSize;
        this.tiles = new ArrayList<>(totalCells);

        for (int i = 1; i < totalCells; i++) {
            this.tiles.add(new Tile(i));
        }
        this.tiles.add(null);

        shuffle(boardSize * boardSize * 15);
    }

    public FixedTaquinGame(UUID id, UUID playerId, int boardSize, List<Integer> tileValues) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.playerId = Objects.requireNonNull(playerId, "playerId must not be null");
        this.boardSize = boardSize;
        int totalCells = boardSize * boardSize;
        this.tiles = new ArrayList<>(totalCells);
        for (Integer val : tileValues) {
            if (val == null || val == 0) {
                this.tiles.add(null);
            } else {
                this.tiles.add(new Tile(val));
            }
        }
    }

    public void shuffle(int count) {
        Random random = ThreadLocalRandom.current();
        int emptyIndex = indexOfNull();
        int lastMovedIndex = -1;

        for (int i = 0; i < count; i++) {
            CellPosition emptyPos = indexToPosition(emptyIndex);
            List<Integer> neighborIndices = validNeighborIndices(emptyPos);
            if (neighborIndices.size() > 1 && neighborIndices.contains(lastMovedIndex)) {
                neighborIndices.remove(Integer.valueOf(lastMovedIndex));
            }
            int chosenIndex = neighborIndices.get(random.nextInt(neighborIndices.size()));
            slideTile(chosenIndex, emptyIndex);
            lastMovedIndex = chosenIndex;
            emptyIndex = chosenIndex;
        }

        if (isSolved()) {
            CellPosition emptyPos = indexToPosition(emptyIndex);
            List<Integer> neighborIndices = validNeighborIndices(emptyPos);
            int chosenIndex = neighborIndices.get(0);
            slideTile(chosenIndex, emptyIndex);
        }
    }

    @Override
    public @NotNull UUID getId() {
        return id;
    }

    @Override
    public @NotBlank String getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public @NotEmpty Set<UUID> getPlayerIds() {
        return Set.of(playerId);
    }

    @Override
    public @NotNull GameStatus getStatus() {
        return isSolved() ? GameStatus.TERMINATED : GameStatus.ONGOING;
    }

    @Override
    public UUID getCurrentPlayerId() {
        return playerId;
    }

    @Override
    public @Min(2) int getBoardSize() {
        return boardSize;
    }

    @Override
    public @NotNull Map<CellPosition, Token> getBoard() {
        Map<CellPosition, Token> board = new LinkedHashMap<>();
        for (int i = 0; i < tiles.size(); i++) {
            Tile tile = tiles.get(i);
            if (tile != null) {
                board.put(indexToPosition(i), tile);
            }
        }
        return Collections.unmodifiableMap(board);
    }

    @Override
    public @NotNull Collection<Token> getRemainingTokens() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<Token> getRemovedTokens() {
        return Collections.emptyList();
    }

    private boolean isSolved() {
        int totalCells = boardSize * boardSize;
        if (tiles.get(totalCells - 1) != null) {
            return false;
        }
        for (int i = 0; i < totalCells - 1; i++) {
            Tile tile = tiles.get(i);
            if (tile == null || tile.value != i + 1) {
                return false;
            }
        }
        return true;
    }

    private int indexOfNull() {
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i) == null) {
                return i;
            }
        }
        return -1;
    }

    private CellPosition unoccupiedPosition() {
        int idx = indexOfNull();
        if (idx < 0) {
            throw new IllegalStateException("Aucune case vide trouvée dans le Taquin");
        }
        return indexToPosition(idx);
    }

    private CellPosition positionOf(int value) {
        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            if (t != null && t.value == value) {
                return indexToPosition(i);
            }
        }
        return null;
    }

    private int indexOf(CellPosition pos) {
        return pos.y() * boardSize + pos.x();
    }

    private CellPosition indexToPosition(int index) {
        return new CellPosition(index % boardSize, index / boardSize);
    }

    private static boolean areNeighbors(CellPosition a, CellPosition b) {
        if (a == null || b == null) return false;
        return (Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y())) == 1;
    }

    private List<Integer> validNeighborIndices(CellPosition pos) {
        List<Integer> list = new ArrayList<>(4);
        int x = pos.x();
        int y = pos.y();
        if (x > 0) list.add(indexOf(new CellPosition(x - 1, y)));
        if (x < boardSize - 1) list.add(indexOf(new CellPosition(x + 1, y)));
        if (y > 0) list.add(indexOf(new CellPosition(x, y - 1)));
        if (y < boardSize - 1) list.add(indexOf(new CellPosition(x, y + 1)));
        return list;
    }

    private void slideTile(int sourceIndex, int destinationIndex) {
        Tile sourceTile = tiles.get(sourceIndex);
        tiles.set(destinationIndex, sourceTile);
        tiles.set(sourceIndex, null);
    }

    private void moveTileTo(Tile tile, CellPosition destination) throws InvalidPositionException {
        CellPosition currentPos = positionOf(tile.value);
        if (currentPos == null) {
            throw new InvalidPositionException("Jeton introuvable sur le plateau");
        }
        if (destination.x() < 0 || destination.y() < 0 || destination.x() >= boardSize || destination.y() >= boardSize) {
            throw new InvalidPositionException("Position hors limites");
        }
        if (!areNeighbors(currentPos, destination)) {
            throw new InvalidPositionException("La tuile n'est pas adjacente à la case cible");
        }
        int destIdx = indexOf(destination);
        if (tiles.get(destIdx) != null) {
            throw new InvalidPositionException("La case cible n'est pas vide");
        }
        int srcIdx = indexOf(currentPos);
        slideTile(srcIdx, destIdx);
    }

    public final class Tile implements Token {
        private final int value;

        private Tile(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public @NotNull Optional<UUID> getOwnerId() {
            return Optional.of(playerId);
        }

        @Override
        public @NotBlank String getName() {
            return String.valueOf(value);
        }

        @Override
        public CellPosition getPosition() {
            return positionOf(value);
        }

        @Override
        public @NotNull Set<CellPosition> getAllowedMoves() {
            CellPosition current = positionOf(value);
            if (current == null) {
                return Collections.emptySet();
            }
            CellPosition unoccupied = unoccupiedPosition();
            if (areNeighbors(current, unoccupied)) {
                return Set.of(unoccupied);
            }
            return Collections.emptySet();
        }

        @Override
        public void moveTo(@NotNull CellPosition destination) throws InvalidPositionException {
            Objects.requireNonNull(destination, "destination must not be null");
            moveTileTo(this, destination);
        }
    }
}
