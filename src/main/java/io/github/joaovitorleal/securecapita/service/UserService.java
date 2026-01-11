package io.github.joaovitorleal.securecapita.service;

import io.github.joaovitorleal.securecapita.domain.User;
import io.github.joaovitorleal.securecapita.dto.UserDto;

public interface UserService {
    UserDto createUser(User user);

    UserDto getUserByEmail(String email);

    void sendVerificationCode(UserDto userDTO);
}
