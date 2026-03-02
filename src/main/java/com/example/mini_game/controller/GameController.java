package com.example.mini_game.controller;

import com.example.mini_game.dto.GuessRequest;
import com.example.mini_game.dto.GuessResponse;
import com.example.mini_game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/guess")
    public ResponseEntity<GuessResponse> guess(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody GuessRequest request) {

        String keycloakId = jwt.getSubject();

        GuessResponse response = gameService.guessNumber(keycloakId, request.getNumber());

        return ResponseEntity.ok(response);
    }

}