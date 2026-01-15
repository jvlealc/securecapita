package io.github.joaovitorleal.securecapita.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MfaVerificationRequestDto(
        @Email(message = "Invalid E-mail.")
        @NotBlank(message = "Email cannot be empty.")
        String email,

        @NotBlank(message = "Code must not be empty.")
        String code
) {
}
