package io.github.joaovitorleal.securecapita.query;

public class RoleQuery {
    public static final String INSERT_ROLE_TO_USER_QUERY = "INSERT INTO user_roles (user_id, role_id) VALUES (:userId, :roleId)";
    public static final String SELECT_ROLE_BY_NAME_QUERY = "SELECT * FROM roles WHERE name = :name";
    public static final String SELECT_ROLE_BY_USER_ID_QUERY = """
                SELECT r.id, r.name, r.permission
                FROM roles r
                JOIN user_roles ur ON ur.role_id = r.id
                JOIN users u ON ur.user_id = u.id
                WHERE u.id = :userId
            """;
}
