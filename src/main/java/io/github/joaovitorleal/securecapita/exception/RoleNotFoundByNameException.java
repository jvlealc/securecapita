package io.github.joaovitorleal.securecapita.exception;

public class RoleNotFoundByNameException extends ApiException {

    public RoleNotFoundByNameException(String roleName) {
        super("Role not found with name: " + roleName);
    }
}
