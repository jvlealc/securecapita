package io.github.joaovitorleal.securecapita.service.implementation;

import io.github.joaovitorleal.securecapita.domain.CustomUserDetails;
import io.github.joaovitorleal.securecapita.domain.Role;
import io.github.joaovitorleal.securecapita.domain.User;
import io.github.joaovitorleal.securecapita.repository.RoleRepository;
import io.github.joaovitorleal.securecapita.repository.UserRepository;
import io.github.joaovitorleal.securecapita.repository.implementation.RoleRepositoryImpl;
import io.github.joaovitorleal.securecapita.repository.implementation.UserRepositoryImpl;
import io.github.joaovitorleal.securecapita.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomUserDetailsServiceImpl.class);

    private final UserRepository<User> userRepository;
    private final RoleRepository<Role> roleRepository;

    public CustomUserDetailsServiceImpl(UserRepository<User> userRepository, RoleRepository<Role> roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * @param username the username identifying the user whose data is required.
     * @return {@link CustomUserDetails}
     * @throws UsernameNotFoundException when no user with the specified username is found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(username);
        if (user == null) {
            LOGGER.error("User not found for username: {}", username);
            throw new UsernameNotFoundException("Not found user with username '" + username + "'.");
        }
        LOGGER.info("Found user in the database: '{}'", user.getEmail());

        return new CustomUserDetails(user, roleRepository.getRoleByUserId(user.getId()).getPermission());
    }
}
