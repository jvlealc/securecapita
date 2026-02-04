package io.github.joaovitorleal.securecapita.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
        @NotBlank(message = "First name is required.")
        @Size(min = 2, max = 40, message = "First name must be between 2 and 40 characters.")
        String firstName,

        @NotBlank(message = "Last name is required.")
        @Size(min = 2, max = 40, message = "Last name must be between 2 and 40 characters.")
        String lastName,

        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email format.")
        @Size(max = 100, message = "Email cannot exceed 100 characters.")
        String email,

        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters.")
        @Pattern( // Ao menos um dígito, uma minúscula, uma maiúscula, um caractere especial, sem espaços em branco.
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character."
        )
        String password
) { }