package io.github.joaovitorleal.securecapita.repository;

import io.github.joaovitorleal.securecapita.domain.TwoFactorVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TwoFactorVerificationJpaRepository extends JpaRepository<TwoFactorVerification, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM TwoFactorVerification t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
