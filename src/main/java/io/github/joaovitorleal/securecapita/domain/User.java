package io.github.joaovitorleal.securecapita.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    @NotEmpty(message = "First name must not be empty.")
    private String firstName;
    @NotEmpty(message = "Last name must not be empty.")
    private String lastName;
    @Email(message = "Invalid E-mail.")
    private String email;
    @NotEmpty(message = "Password must not be empty.")
    private String password;
    private String phone;
    private String address;
    private String title;
    private String bio;
    private boolean enabled = false;
    private boolean isNonLocked = true;
    private boolean isUsingMfa = false;
    private String imageUrl;
    private LocalDateTime createdAt;
}
