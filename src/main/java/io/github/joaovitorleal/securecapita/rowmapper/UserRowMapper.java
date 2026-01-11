package io.github.joaovitorleal.securecapita.rowmapper;

import io.github.joaovitorleal.securecapita.domain.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    /**
     * @param rs     the {@code ResultSet} to map (pre-initialized for the current row) 
     * @param rowNum the number of the current row
     * @return {@link User}
     * @throws SQLException if an SQL access error occurs while reading the ResultSet
     */
    @Nullable
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .phone(rs.getString("phone"))
                .address(rs.getString("address"))
                .title(rs.getString("title"))
                .bio(rs.getString("bio"))
                .enabled(rs.getBoolean("enabled"))
                .nonLocked(rs.getBoolean("non_locked"))
                .usingMfa(rs.getBoolean("using_mfa"))
                .imageUrl(rs.getString("image_url"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
