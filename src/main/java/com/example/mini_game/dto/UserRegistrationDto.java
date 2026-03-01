package com.example.mini_game.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank
    private String username;
    @Email
    @NotBlank private String email;
    @NotBlank @Size(min = 8) private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
