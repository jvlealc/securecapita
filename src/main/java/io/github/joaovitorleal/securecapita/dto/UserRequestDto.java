package io.github.joaovitorleal.securecapita.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record UserRequestDto(
        @NotEmpty(message = "First name must not be empty.")
        String firstName,
        @NotEmpty(message = "Last name must not be empty.")
        String lastName,
        @Email(message = "Invalid E-mail.")
        String email,
        @NotEmpty(message = "Password must not be empty.")
        String password
) {
}
