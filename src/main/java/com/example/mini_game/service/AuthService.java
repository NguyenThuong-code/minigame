package com.example.mini_game.service;

import com.example.mini_game.dto.LoginRequestDto;
import com.example.mini_game.dto.LoginResponseDto;
import com.example.mini_game.dto.UserProfileDto;
import com.example.mini_game.dto.UserRegistrationDto;
import com.example.mini_game.entity.Role;
import com.example.mini_game.entity.UserProfile;
import com.example.mini_game.mapper.UserProfileMapper;
import com.example.mini_game.repo.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserProfileRepository userProfileRepository;
    private final KeycloakService keycloakService;
    private final UserProfileMapper mapper;

    @Transactional
    public UserProfileDto register(UserRegistrationDto dto) {
        if (userProfileRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        String keycloakId = keycloakService.createUser(dto, Role.USER);

        UserProfile profile = mapper.toEntity(dto);

        profile.setKeycloakId(keycloakId);
        profile.setTurns(0);
        profile.setScore(0);
        profile.setTotalGuesses(0);
        profile.setRole(Role.USER);

        userProfileRepository.save(profile);

        return mapper.toDto(profile);
    }

    public LoginResponseDto login(LoginRequestDto dto) {
        return keycloakService.login(dto);
    }

}