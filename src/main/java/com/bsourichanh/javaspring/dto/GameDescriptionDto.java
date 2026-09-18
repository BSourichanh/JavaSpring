package com.bsourichanh.javaspring.dto;

public record GameDescriptionDto(
        String id,
        String name,
        int defaultPlayerCount,
        int defaultBoardSize
) {}
