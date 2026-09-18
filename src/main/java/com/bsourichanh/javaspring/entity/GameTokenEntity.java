package com.bsourichanh.javaspring.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "game_tokens")
public class GameTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String ownerId;

    public String name;

    public boolean removed;

    public Integer x;

    public Integer y;

    public GameTokenEntity() {}

    public GameTokenEntity(String ownerId, String name, boolean removed, Integer x, Integer y) {
        this.ownerId = ownerId;
        this.name = name;
        this.removed = removed;
        this.x = x;
        this.y = y;
    }
}
