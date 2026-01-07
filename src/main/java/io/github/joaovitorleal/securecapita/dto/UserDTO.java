package io.github.joaovitorleal.securecapita.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private String address;
    private String title;
    private String bio;
    private boolean enabled;
    private boolean nonLocked;
    private boolean usingMfa;
    private String imageUrl;
    private LocalDateTime createdAt;
}
