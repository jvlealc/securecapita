package io.github.joaovitorleal.securecapita.repository.implementation;

import io.github.joaovitorleal.securecapita.domain.CustomUserDetails;
import io.github.joaovitorleal.securecapita.domain.Role;
import io.github.joaovitorleal.securecapita.domain.User;
import io.github.joaovitorleal.securecapita.exception.ApiException;
import io.github.joaovitorleal.securecapita.exception.EmailAlreadyExistsException;
import io.github.joaovitorleal.securecapita.exception.NotFoundUserByEmailException;
import io.github.joaovitorleal.securecapita.repository.RoleRepository;
import io.github.joaovitorleal.securecapita.repository.UserRepository;
import io.github.joaovitorleal.securecapita.rowmapper.UserRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.github.joaovitorleal.securecapita.enumeration.RoleType.ROLE_USER;
import static io.github.joaovitorleal.securecapita.enumeration.VerificationType.ACCOUNT;
import static io.github.joaovitorleal.securecapita.query.UserQuery.*;
import static java.util.Objects.requireNonNull;

@Repository
public class UserRepositoryImpl implements UserRepository<User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryImpl.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RoleRepository<Role> roleRepository;
    private final PasswordEncoder encoder;

    public UserRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate, RoleRepository<Role> roleRepository, PasswordEncoder encoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    @Transactional
    @Override
    public User save(User user) {
        if (getEmailCount(user.getEmail().trim().toLowerCase()) > 0) {
            throw new EmailAlreadyExistsException("Email already exists. Please use a different email and try again.");
        }
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            SqlParameterSource parameters = getSqlParametersSource(user);
            jdbcTemplate.update(INSERT_USER_QUERY, parameters, keyHolder);
            user.setId(requireNonNull(keyHolder.getKey()).longValue());
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());
            jdbcTemplate.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), "url", verificationUrl));
            //emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT.getType());
            user.setEnabled(false);
            user.setNonLocked(true);
            user.setUsingMfa(false);
            return user;
        } catch (Exception e) {
            LOGGER.error("Error saving user: {}", e.getMessage(), e);
            throw new ApiException("An error occurred. Please try again later.", e);
        }
    }

    @Override
    public Collection findAll(int page, int pageSize) {
        return List.of();
    }

    @Override
    public User findById(Long id) {
        return null;
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean deleteById(Long id) {
        return null;
    }

    @Override
    public User findUserByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject(SELECT_USER_BY_EMAIL, Map.of("email", email), new UserRowMapper());
        }  catch (EmptyResultDataAccessException e) {
            LOGGER.error("Error when retrieving user by username '{}'. Error: {}", email, e.getMessage(), e);
            throw new NotFoundUserByEmailException("No User found by email " + email);
        }  catch (Exception e) {
            LOGGER.error("An error occurred when retrieving user by username '{}'. Error: {}", email, e.getMessage(), e);
            throw new ApiException("An error occurred. Please try again later.", e);
        }
    }

    private Integer getEmailCount(String userEmail) {
        return jdbcTemplate.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email", userEmail), Integer.class);
    }

    private SqlParameterSource getSqlParametersSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", encoder.encode(user.getPassword()));
    }

    private String getVerificationUrl(String key, String type) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/user/verify/" + type + "/" + key).toString();
    }
}
