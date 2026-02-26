package io.github.jvlealc.securecapita.mapper;

import io.github.jvlealc.securecapita.domain.User;
import io.github.jvlealc.securecapita.dto.UserCreateRequestDto;
import io.github.jvlealc.securecapita.dto.UserResponseDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserMapper {

    private final RoleMapper roleMapper;

    public UserMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
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
                user.getMfaType(),
                user.getImageUrl(),
                user.getCreatedAt(),
                roleMapper.toDto(user.getRole())
        );
    }

    public User toEntity(UserCreateRequestDto userCreateDto) {
        User user = new User();
        BeanUtils.copyProperties(userCreateDto, user);
        return user;
    }
}
