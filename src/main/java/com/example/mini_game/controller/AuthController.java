package com.example.mini_game.controller;

import com.example.mini_game.dto.LoginRequestDto;
import com.example.mini_game.dto.LoginResponseDto;
import com.example.mini_game.dto.UserProfileDto;
import com.example.mini_game.dto.UserRegistrationDto;
import com.example.mini_game.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

     @PostMapping("/register")
        public ResponseEntity<UserProfileDto> register(@Valid @RequestBody UserRegistrationDto dto) {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}