package com.example.mini_game.service;

import com.example.mini_game.dto.LoginRequestDto;
import com.example.mini_game.dto.LoginResponseDto;
import com.example.mini_game.dto.UserRegistrationDto;
import com.example.mini_game.entity.Role;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    @Value("${keycloak.server-url}") private String serverUrl;
    @Value("${keycloak.realm}")      private String realm;
    @Value("${keycloak.client-id}")  private String clientId;
    @Value("${keycloak.client-secret}") private String clientSecret;
    @Value("${keycloak.admin-username}") private String adminUsername;
    @Value("${keycloak.admin-password}") private String adminPassword;

    private final RestTemplate restTemplate;

    private Keycloak getAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    public String createUser(UserRegistrationDto dto, Role role) {
        try (Keycloak keycloak = getAdminClient()) {
            UsersResource usersResource = keycloak.realm(realm).users();
            UserRepresentation user = getUserRepresentation(dto);

            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                String body = response.readEntity(String.class);
                throw new RuntimeException("Keycloak user creation failed [" + response.getStatus() + "]: " + body);
            }

            String location = response.getHeaderString("Location");
            String keycloakId = location.substring(location.lastIndexOf("/") + 1);

            assignRole(keycloakId, role.name(), keycloak);

            return keycloakId;
        }
    }

    private static UserRepresentation getUserRepresentation(UserRegistrationDto dto) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(dto.getPassword());
        credential.setTemporary(false);

        UserRepresentation user = new UserRepresentation();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setCredentials(List.of(credential));
        return user;
    }

    private void assignRole(String keycloakId, String roleName, Keycloak keycloak) {
        RealmResource realmResource = keycloak.realm(realm);
        RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
        realmResource.users().get(keycloakId).roles().realmLevel().add(List.of(role));
    }

    public LoginResponseDto login(LoginRequestDto dto) {
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", dto.getUsername());
        body.add("password", dto.getPassword());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> tokenData = response.getBody();

            return LoginResponseDto.builder()
                    .accessToken((String) tokenData.get("access_token"))
                    .refreshToken((String) tokenData.get("refresh_token"))
                    .expiresIn(((Number) tokenData.get("expires_in")).longValue())
                    .tokenType("Bearer")
                    .build();

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("Invalid username or password");
            }
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    public void logout(String refreshToken) {
        String logoutUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/logout";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);

        restTemplate.postForEntity(logoutUrl, new HttpEntity<>(body, headers), Void.class);
    }
}