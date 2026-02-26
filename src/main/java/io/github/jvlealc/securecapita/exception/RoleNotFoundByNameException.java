package io.github.jvlealc.securecapita.exception;

public class RoleNotFoundByNameException extends ResourceNotFoundException {

    public RoleNotFoundByNameException(String roleName) {
        super("Role not found with name: " + roleName);
    }
}
