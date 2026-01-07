package io.github.joaovitorleal.securecapita.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginForm {


    @Email(message = "Invalid E-mail.")
    @NotBlank(message = "E-mail must not be empty.")
    private String email;
    @NotBlank(message = "Password must not be empty.")
    private String password;

    public LoginForm() {
    }

    public LoginForm(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
