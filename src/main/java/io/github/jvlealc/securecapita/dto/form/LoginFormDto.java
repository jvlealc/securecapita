package io.github.jvlealc.securecapita.dto.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginFormDto(
        @Email(message = "Invalid E-mail.")
        @NotBlank(message = "E-mail must not be empty.")
        String email,

        @NotBlank(message = "Password must not be empty.")
        String password
) {
}
