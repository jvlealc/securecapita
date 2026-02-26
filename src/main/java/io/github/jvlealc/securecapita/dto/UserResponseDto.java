package io.github.jvlealc.securecapita.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.jvlealc.securecapita.domain.enums.MfaType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
        MfaType mfaType,
        String imageUrl,
        LocalDateTime createdAt,
        RoleDto role
) {
}
