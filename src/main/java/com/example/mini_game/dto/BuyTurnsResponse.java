package com.example.mini_game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class BuyTurnsResponse {
    private int addedTurns;
    private int totalTurns;
    private String message;
}