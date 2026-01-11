package io.github.joaovitorleal.securecapita.dto;

import java.time.LocalDateTime;

public record UserResponseDto(

        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String title,
        String bio,
        boolean enabled,
        boolean nonLocked,
        boolean usingMfa,
        String imageUrl,
        LocalDateTime createdAt
) {
}
