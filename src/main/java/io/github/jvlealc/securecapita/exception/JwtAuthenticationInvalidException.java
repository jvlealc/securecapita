package io.github.jvlealc.securecapita.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationInvalidException extends AuthenticationException {

    public JwtAuthenticationInvalidException(String message) {
        super(message);
    }

    public JwtAuthenticationInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
