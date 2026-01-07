package io.github.joaovitorleal.securecapita.service.implementation;

import io.github.joaovitorleal.securecapita.domain.User;
import io.github.joaovitorleal.securecapita.dto.UserDTO;
import io.github.joaovitorleal.securecapita.dtomapper.UserMapper;
import io.github.joaovitorleal.securecapita.repository.UserRepository;
import io.github.joaovitorleal.securecapita.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository<User> userRepository;

    public UserServiceImpl(UserRepository<User> userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO createUser(User user) {
        return UserMapper.toUserDTO(userRepository.save(user));
    }

    /**
     * @param email user email (username)
     * @return {@link UserDTO}
     */
    @Override
    public UserDTO getUserByEmail(String email) {
        return UserMapper.toUserDTO(userRepository.findUserByEmail(email));
    }
}
