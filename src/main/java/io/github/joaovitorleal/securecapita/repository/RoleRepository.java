package io.github.joaovitorleal.securecapita.repository;

import io.github.joaovitorleal.securecapita.domain.Role;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface RoleRepository<T extends Role> {
    /* Basic CRUD operations */
    T save(T data);
    Collection<T> findAll(int page, int pageSize);
    T findById(Long id);
    T update(T data);
    Boolean deleteById(Long id);

    /* Others operations */
    void addRoleToUser(Long userId, String roleName);
    Role getRoleByUserId(Long userId);
    Role getRoleByUserEmail(String userEmail);
    void updateUserRole(Long userId, String roleName);
}
