package io.github.joaovitorleal.securecapita.dtomapper;

import io.github.joaovitorleal.securecapita.domain.User;
import io.github.joaovitorleal.securecapita.dto.UserDto;
import io.github.joaovitorleal.securecapita.dto.UserRequestDto;
import io.github.joaovitorleal.securecapita.dto.UserResponseDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserMapper {

    public UserDto toUserDto(User user) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }

    public UserResponseDto toResponseDto(User user) {
        Objects.requireNonNull(user, "The entity User must not be null when convert to DTO.");

        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getTitle(),
                user.getBio(),
                user.isEnabled(),
                user.isNonLocked(),
                user.isUsingMfa(),
                user.getImageUrl(),
                user.getCreatedAt()
        );
    }

    public User toUser(UserDto userDto) {
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        return user;
    }

    public User toUserEntity(UserRequestDto userDto) {
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        return user;
    }
}
