package com.bsourichanh.javaspring.controller;

import com.bsourichanh.javaspring.dto.GameCreationParams;
import com.bsourichanh.javaspring.dto.MoveDto;
import com.bsourichanh.javaspring.security.JwtService;
import com.bsourichanh.javaspring.service.GameService;
import com.bsourichanh.javaspring.service.UserValidationService;
import fr.le_campus_numerique.square_games.engine.Game;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@RestController
public class GameController {

    private final GameService gameService;
    private final UserValidationService userValidationService;
    private final JwtService jwtService;

    public GameController(GameService gameService, UserValidationService userValidationService, JwtService jwtService) {
        this.gameService = gameService;
        this.userValidationService = userValidationService;
        this.jwtService = jwtService;
    }

    private String resolveUserId(String xUserId, String authHeader) {
        // 1. Priorité au JWT via Bearer token : validation locale instantanée (0 appel réseau vers User Service)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.isTokenValid(token)) {
                return jwtService.extractUserId(token);
            }
            return null;
        }

        // 2. Vérification dans le SecurityContextHolder
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }

        // 3. Fallback rétro-compatible (Itération 4) : appel distant via X-UserId
        if (xUserId != null && !xUserId.isBlank() && userValidationService.isValidUser(xUserId)) {
            return xUserId;
        }

        return null;
    }

    @PostMapping("/games")
    public ResponseEntity<?> createGame(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-UserId", required = false) String userId,
            @RequestBody GameCreationParams params) {

        String resolvedUserId = resolveUserId(userId, authHeader);
        if (resolvedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentification requise (Bearer JWT ou X-UserId valide)"));
        }
        return ResponseEntity.ok(gameService.createGame(params, resolvedUserId));
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<?> getGame(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-UserId", required = false) String userId,
            @PathVariable("gameId") UUID gameId) {

        String resolvedUserId = resolveUserId(userId, authHeader);
        if (resolvedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentification requise (Bearer JWT ou X-UserId valide)"));
        }
        Game game = gameService.getGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    @GetMapping("/games")
    public ResponseEntity<?> getGames(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-UserId", required = false) String userId) {

        String resolvedUserId = resolveUserId(userId, authHeader);
        if (resolvedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentification requise (Bearer JWT ou X-UserId valide)"));
        }
        Collection<Game> games = gameService.getGamesForUser(resolvedUserId);
        return ResponseEntity.ok(games);
    }

    @PostMapping("/games/{gameId}/moves")
    public ResponseEntity<?> playMove(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-UserId", required = false) String userId,
            @PathVariable("gameId") UUID gameId,
            @RequestBody MoveDto move) {

        String resolvedUserId = resolveUserId(userId, authHeader);
        if (resolvedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentification requise (Bearer JWT ou X-UserId valide)"));
        }

        Game game = gameService.getGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }

        // Seul currentPlayerId a le droit de jouer
        if (game.getCurrentPlayerId() == null || !game.getCurrentPlayerId().toString().equals(resolvedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Ce n'est pas votre tour de jouer"));
        }

        try {
            Game updated = gameService.playMove(gameId, resolvedUserId, move.x(), move.y());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
