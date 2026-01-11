package io.github.joaovitorleal.securecapita.service;

import io.github.joaovitorleal.securecapita.domain.User;
import io.github.joaovitorleal.securecapita.dto.UserDto;
import io.github.joaovitorleal.securecapita.dto.UserRequestDto;
import io.github.joaovitorleal.securecapita.dtomapper.UserMapper;
import io.github.joaovitorleal.securecapita.repository.RoleJpaRepository;
import io.github.joaovitorleal.securecapita.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Order(value = Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class UserService2 {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;
    private final RoleJpaRepository roleJpaRepository;
    private final PasswordEncoder encoder;

    @Transactional
    public UserDto createUser(UserRequestDto userRequestDto) {
        User user = userMapper.toUserEntity(userRequestDto);
        User createdUser = userJpaRepository.save(user);

    }
}
