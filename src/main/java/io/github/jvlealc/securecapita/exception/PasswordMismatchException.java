package io.github.jvlealc.securecapita.exception;

/**
 * Utilizado na redefinição de senha via Token/URL.
 * Usuário não autenticado.
 * */
public class PasswordMismatchException extends ApiException {

    public PasswordMismatchException(String message) {
        super(message);
    }
}
