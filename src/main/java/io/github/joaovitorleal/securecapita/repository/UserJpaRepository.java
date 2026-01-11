package io.github.joaovitorleal.securecapita.repository;

import io.github.joaovitorleal.securecapita.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    String COUNT_USER_EMAIL_QUERY = "SELECT COUNT(*) FROM users WHERE email = :email";
    String INSERT_USER_QUERY = "INSERT INTO users (first_name, last_name, email, password) VALUES (:firstName, :lastName, :email, :password)";
    String INSERT_ACCOUNT_VERIFICATION_URL_QUERY = "INSERT INTO AccountVerifications (user_id, url) VALUES (:userId, :url)";
    String SELECT_USER_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = :email";
    String DELETE_VERIFICATION_CODE_BY_USER_ID_QUERY = "DELETE FROM two_factor_verifications WHERE user_id = :userId";
    String INSERT_VERIFICATION_CODE_QUERY = "INSERT INTO two_factor_verifications (user_id, code, expiration_date) VALUES (:userId, :code, :expirationDate) WHERE user_id = :userId";

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
