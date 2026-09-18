package com.bsourichanh.javaspring.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GameCreationParams(
        @JsonProperty("gameType")
        @JsonAlias({"type", "game"})
        String gameType,

        @JsonProperty("playerCount")
        @JsonAlias({"nbPlayers", "players"})
        Integer playerCount,

        @JsonProperty("boardSize")
        @JsonAlias({"totalCell", "size"})
        Integer boardSize
) {
    public String getType() {
        return gameType;
    }

    public Integer getNbPlayers() {
        return playerCount;
    }

    public Integer getTotalCell() {
        return boardSize;
    }
}
