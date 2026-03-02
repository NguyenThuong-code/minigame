package com.example.mini_game.service;

import com.example.mini_game.dto.BuyTurnsResponse;
import com.example.mini_game.dto.GuessResponse;
import com.example.mini_game.entity.UserProfile;
import com.example.mini_game.repo.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class GameService {

    private final UserProfileRepository userProfileRepository;
    private final ThreadLocalRandom rng = ThreadLocalRandom.current();
    private static final int BUY_TURNS_AMOUNT = 5;
    private static final double WIN_RATE = 0.05;
    private final TxAfterCommit txAfterCommit;
    private final LeaderboardRedisService leaderboardRedisService;


    @Transactional
    public GuessResponse guessNumber(String keycloakId, int userGuess) {
        if (userGuess < 1 || userGuess > 5) {
            throw new IllegalArgumentException("Number must be between 1 and 5");
        }

        UserProfile user = userProfileRepository.findByKeycloakIdForUpdate(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTurns() <= 0) {
            return GuessResponse.builder()
                    .correct(false)
                    .randomNumber(0)
                    .remainingTurns(0)
                    .currentScore(user.getScore())
                    .message("You have no turns left!")
                    .build();
        }

        user.setTurns(user.getTurns() - 1);
        user.setTotalGuesses(user.getTotalGuesses() + 1);
        boolean win = rng.nextDouble() < WIN_RATE;

        int randomNumber;
        boolean correct;

        if (win) {
            randomNumber = userGuess;
            correct = true;

            user.setScore(user.getScore() + 1);
        } else {
            randomNumber = randomDifferentFrom(userGuess);
            correct = false;
        }

        userProfileRepository.save(user);
        txAfterCommit.run(() -> {
            leaderboardRedisService.updateWinRate(
                    keycloakId,
                    user.getUsername(),
                    user.getScore(),
                    user.getTotalGuesses()
            );
        });
        return GuessResponse.builder()
                .correct(correct)
                .randomNumber(randomNumber)
                .remainingTurns(user.getTurns())
                .currentScore(user.getScore())
                .message(correct ? "Correct!" : "Wrong!")
                .build();
    }
    private int randomDifferentFrom(int guess) {
       int r = rng.nextInt(1, 5);
        return (r >= guess) ? r + 1 : r;
    }
}