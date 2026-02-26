package io.github.jvlealc.securecapita.repository;

import io.github.jvlealc.securecapita.domain.AccountVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountVerificationJpaRepository extends JpaRepository<AccountVerification, Long> {

    Optional<AccountVerification> findByUrl(String url);
    void deleteByUserId(Long userId);
}
