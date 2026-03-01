package com.example.mini_game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class GuessResponse {
    private boolean correct;
    private int randomNumber;
    private int remainingTurns;
    private int currentScore;
    private String message;
}