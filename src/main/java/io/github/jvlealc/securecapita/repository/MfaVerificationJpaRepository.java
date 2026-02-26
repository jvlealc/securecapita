package io.github.jvlealc.securecapita.repository;

import io.github.jvlealc.securecapita.domain.MfaVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MfaVerificationJpaRepository extends JpaRepository<MfaVerification, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM MfaVerification tfv WHERE tfv.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT tfv FROM MfaVerification tfv WHERE tfv.user.id = :userId")
    Optional<MfaVerification> findByUserId(@Param("userId") Long userId);
}
