package io.github.joaovitorleal.securecapita.repository;

import io.github.joaovitorleal.securecapita.domain.AccountVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountVerificationJpaRepository extends JpaRepository<AccountVerification, Long> {
}
