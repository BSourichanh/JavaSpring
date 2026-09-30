package com.bsourichanh.javaspring.dto;

import jakarta.validation.constraints.NotNull;

public record MoveDto(
        @NotNull(message = "La coordonnée x est obligatoire")
        Integer x,
        @NotNull(message = "La coordonnée y est obligatoire")
        Integer y
) {}
