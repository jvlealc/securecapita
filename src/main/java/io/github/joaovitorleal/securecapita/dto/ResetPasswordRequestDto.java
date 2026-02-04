package io.github.joaovitorleal.securecapita.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
        @NotBlank(message = "Password is required.")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters.")
        @Pattern( // Ao menos um dígito, uma minúscula, uma maiúscula, um caractere especial, sem espaços em branco.
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",
                message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character."
        )
        String newPassword,

        @NotBlank(message = "Confirm password is required.")
        String confirmPassword
) {
}
