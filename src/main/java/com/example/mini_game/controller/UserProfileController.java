package com.example.mini_game.controller;

import com.example.mini_game.dto.LeaderboardItemDto;
import com.example.mini_game.dto.MeDto;
import com.example.mini_game.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserProfileService service;

    @GetMapping("/leaderboard")
    public List<LeaderboardItemDto> leaderboard() {
        return service.getTop10Leaderboard();
    }

    @GetMapping("/me")
    public MeDto profile(@AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        return service.getProfile(keycloakId);
    }
}