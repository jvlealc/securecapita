package io.github.joaovitorleal.securecapita.repository.implementation;

import io.github.joaovitorleal.securecapita.domain.Role;
import io.github.joaovitorleal.securecapita.exception.ApiException;
import io.github.joaovitorleal.securecapita.exception.NotFoundRoleByUserIdException;
import io.github.joaovitorleal.securecapita.exception.NotFoundRoleNameException;
import io.github.joaovitorleal.securecapita.repository.RoleRepository;
import io.github.joaovitorleal.securecapita.rowmapper.RoleRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static io.github.joaovitorleal.securecapita.enumeration.RoleType.ROLE_USER;
import static io.github.joaovitorleal.securecapita.query.RoleQuery.*;
import static java.util.Map.of;

@Repository
public class RoleRepositoryImpl implements RoleRepository<Role> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleRepositoryImpl.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RoleRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Role save(Role data) {
        return null;
    }

    @Override
    public Collection findAll(int page, int pageSize) {
        return List.of();
    }

    @Override
    public Role findById(Long id) {
        return null;
    }

    @Override
    public Role update(Role data) {
        return null;
    }

    @Override
    public Boolean deleteById(Long id) {
        return null;
    }

    @Override
    public void addRoleToUser(Long userId, String roleName) {
        LOGGER.info("Adding role '{}' to user with ID '{}'", roleName, userId);
        try {
            Role role = jdbcTemplate.queryForObject(SELECT_ROLE_BY_NAME_QUERY, of("name", roleName), new RoleRowMapper());
            jdbcTemplate.update(INSERT_ROLE_TO_USER_QUERY, of("userId", userId, "roleId", role.getId()));
        } catch (EmptyResultDataAccessException e) {
            LOGGER.warn("No role found with name='{}'", roleName);
            throw new NotFoundRoleNameException("No role found with name: " + roleName);
        } catch (Exception e) {
            LOGGER.error("Error while trying to add role '{}' to user with ID: {}. Error: {}", roleName, userId, e.getMessage(), e);
            throw new ApiException("An error occurred. Please try again later.", e);
        }
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_ROLE_BY_USER_ID_QUERY, of("userId", userId), new RoleRowMapper());
        } catch (EmptyResultDataAccessException e) {
            LOGGER.warn( "Could not find role with User ID '{}'", userId);
            throw new NotFoundRoleByUserIdException("No role found by User ID: " + userId);
        } catch (Exception e) {
            LOGGER.error("Error while trying to fetching Role by User ID '{}'", userId, e);
            throw new ApiException("An error occurred. Please try again later.", e);
        }
    }

    @Override
    public Role getRoleByUserEmail(String userEmail) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {

    }
}
