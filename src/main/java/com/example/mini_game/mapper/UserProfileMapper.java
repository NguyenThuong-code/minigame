package com.example.mini_game.mapper;

import com.example.mini_game.dto.UserProfileDto;
import com.example.mini_game.dto.UserRegistrationDto;
import com.example.mini_game.entity.UserProfile;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserProfileMapper {

    // Entity -> DTO
    @Mapping(target = "role", expression = "java(p.getRole() != null ? p.getRole().name() : null)")
    UserProfileDto toDto(UserProfile p);

    // Register DTO -> Entity (we will set fields like keycloakId, turns, score in service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "turns", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "role", ignore = true)
    UserProfile toEntity(UserRegistrationDto dto);
}