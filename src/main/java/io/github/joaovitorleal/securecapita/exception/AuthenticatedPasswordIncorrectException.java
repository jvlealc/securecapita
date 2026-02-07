package io.github.joaovitorleal.securecapita.exception;

/**
 * Lançando no sistema de alteração de senha
 * de usuário autenticado.
 * */
public class AuthenticatedPasswordIncorrectException extends ApiException {

    public AuthenticatedPasswordIncorrectException(String message) {
        super(message);
    }
}
