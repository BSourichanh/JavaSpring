package com.bsourichanh.javaspring.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "game_tokens")
public class GameTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ownerId;

    private String name;

    private boolean removed;

    private Integer x;

    private Integer y;

    public GameTokenEntity() {}

    public GameTokenEntity(String ownerId, String name, boolean removed, Integer x, Integer y) {
        this.ownerId = ownerId;
        this.name = name;
        this.removed = removed;
        this.x = x;
        this.y = y;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isRemoved() { return removed; }
    public void setRemoved(boolean removed) { this.removed = removed; }

    public Integer getX() { return x; }
    public void setX(Integer x) { this.x = x; }

    public Integer getY() { return y; }
    public void setY(Integer y) { this.y = y; }
}
