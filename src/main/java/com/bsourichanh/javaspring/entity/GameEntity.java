package com.bsourichanh.javaspring.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    public String id;

    public String factoryId;

    public int boardSize;

    public String status;

    public String currentPlayerId;

    @Column(columnDefinition = "TEXT")
    public String playerIds;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    public List<GameTokenEntity> tokens = new ArrayList<>();

    public GameEntity() {}
}
