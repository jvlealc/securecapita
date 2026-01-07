package io.github.joaovitorleal.securecapita.repository;

import io.github.joaovitorleal.securecapita.domain.User;

import java.util.Collection;

public interface UserRepository<T extends User> {
    /* Basic CRUD operations */
    T save(T data);
    Collection<T> findAll(int page, int pageSize);
    T findById(Long id);
    T update(T data);
    Boolean deleteById(Long id);

    /* Others operations */
    User findUserByEmail(String email);
}
