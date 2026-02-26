package io.github.jvlealc.securecapita.repository;

import io.github.jvlealc.securecapita.domain.ResetPasswordVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ResetPasswordVerificationJpaRepository extends JpaRepository<ResetPasswordVerification, Long> {

    Optional<ResetPasswordVerification> findByUrl(String url);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ResetPasswordVerification rpv WHERE rpv.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
