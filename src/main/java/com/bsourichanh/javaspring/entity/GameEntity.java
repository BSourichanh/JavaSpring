package com.bsourichanh.javaspring.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    private String id;

    private String factoryId;

    private int boardSize;

    private String status;

    private String currentPlayerId;

    @Column(columnDefinition = "TEXT")
    private String playerIds;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private List<GameTokenEntity> tokens = new ArrayList<>();

    public GameEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFactoryId() { return factoryId; }
    public void setFactoryId(String factoryId) { this.factoryId = factoryId; }

    public int getBoardSize() { return boardSize; }
    public void setBoardSize(int boardSize) { this.boardSize = boardSize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentPlayerId() { return currentPlayerId; }
    public void setCurrentPlayerId(String currentPlayerId) { this.currentPlayerId = currentPlayerId; }

    public String getPlayerIds() { return playerIds; }
    public void setPlayerIds(String playerIds) { this.playerIds = playerIds; }

    public List<GameTokenEntity> getTokens() { return tokens; }
    public void setTokens(List<GameTokenEntity> tokens) { this.tokens = tokens; }
}
