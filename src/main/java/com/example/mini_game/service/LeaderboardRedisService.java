package com.example.mini_game.service;

import com.example.mini_game.dto.LeaderboardItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LeaderboardRedisService {

    private static final String ZSET_KEY = "lb:winrate";
    private static final String USER_HASH = "lb:user";
    private static final long SCALE = 1_000_000_000L;

    private final StringRedisTemplate redis;

    /** Update leaderboard for one user */
    public void updateWinRate(String keycloakId, String username, long score, long totalGuesses) {
        redis.opsForHash().put(USER_HASH, keycloakId, username);

        // not enough data -> remove from leaderboard
        if (totalGuesses <= 0 || score <= 0) {
            redis.opsForZSet().remove(ZSET_KEY, keycloakId);
            return;
        }

        double winRate = (double) score / (double) totalGuesses;
        double scaled = (double) ((long) Math.floor(winRate * SCALE));
        redis.opsForZSet().add(ZSET_KEY, keycloakId, scaled);
    }

    public List<LeaderboardItemDto> topTenUsersBoard() {

        Set<ZSetOperations.TypedTuple<String>> tuples =
                redis.opsForZSet().reverseRangeWithScores(ZSET_KEY, 0, 9);

        if (tuples == null || tuples.isEmpty()) return List.of();

        List<LeaderboardItemDto> result = new ArrayList<>(tuples.size());
        for (var t : tuples) {
            String keycloakId = t.getValue();
            double scaled = (t.getScore() == null) ? 0.0 : t.getScore();

            String username = (String) redis.opsForHash().get(USER_HASH, keycloakId);
            if (username == null) username = keycloakId; // fallback
            double winRate = scaled / SCALE;

            result.add(new LeaderboardItemDto(username, winRate));
        }
        return result;
    }
}