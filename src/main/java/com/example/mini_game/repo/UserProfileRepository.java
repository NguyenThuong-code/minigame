package com.example.mini_game.repo;

import com.example.mini_game.entity.UserProfile;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserProfile u where u.keycloakId = :keycloakId")
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "2000")
    })
    Optional<UserProfile> findByKeycloakIdForUpdate(@Param("keycloakId") String keycloakId);

    @Modifying
    @Query("update UserProfile u set u.turns = u.turns + :amount where u.keycloakId = :keycloakId")
    int addTurns(@Param("keycloakId") String keycloakId, @Param("amount") int amount);

    Optional<UserProfile> findByKeycloakId(String keycloakId);
    Optional<UserProfile> findByEmail(String email);

}