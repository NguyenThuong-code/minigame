package com.example.mini_game.service;

import com.example.mini_game.dto.LeaderboardItemDto;
import com.example.mini_game.dto.MeDto;
import com.example.mini_game.entity.UserProfile;
import com.example.mini_game.repo.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository repo;
    private final LeaderboardRedisService leaderboardRedisService;

    public List<LeaderboardItemDto> getTop10Leaderboard() {
        return leaderboardRedisService.topTenUsersBoard();
    }

    public MeDto getProfile(String keycloakId) {
        UserProfile u = repo.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new MeDto(u.getEmail(), u.getScore(), u.getTurns());
    }

}