package com.bsourichanh.javaspring.dto;

public record GameDescriptionDto(
        String id,
        String name,
        Integer defaultPlayerCount,
        Integer defaultBoardSize
) {}
