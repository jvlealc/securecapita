package io.github.joaovitorleal.securecapita.security.service;

import io.github.joaovitorleal.securecapita.domain.Role;
import io.github.joaovitorleal.securecapita.domain.User;
import io.github.joaovitorleal.securecapita.repository.RoleJpaRepository;
import io.github.joaovitorleal.securecapita.repository.UserJpaRepository;
import io.github.joaovitorleal.securecapita.security.model.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserJpaRepository userJpaRepository;

    public CustomUserDetailsService(UserJpaRepository userJpaRepository, RoleJpaRepository roleJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    /**
     * @param username the username identifying the user whose data is required.
     * @return {@link CustomUserDetails}
     * @throws UsernameNotFoundException when no user with the specified username is found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userJpaRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
        if (user == null) {
            LOGGER.error("User not found for username: {}", username);
            throw new UsernameNotFoundException("Not found user with username '" + username + "'.");
        }
        LOGGER.info("Found user in the database: '{}'", user.getEmail());

        return new CustomUserDetails(user, user.getRole().getPermission());
    }
}
