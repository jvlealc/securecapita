package io.github.jvlealc.securecapita.dto;

import jakarta.validation.constraints.*;

public record UserUpdateRequestDto(
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

        @Pattern(regexp = "^\\d+$", message = "Phone must contain only numbers.")
        @Size(min = 7, max = 30, message = "Phone must be between 7 and 30 digits.")
        String phone,

        @Size(max = 50, message = "Title cannot exceed 50 characters.")
        String title,

        @Size(max = 500, message = "Bio cannot exceed 500 characters.")
        String bio
) {
}
