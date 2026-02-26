package io.github.jvlealc.securecapita.mapper;

import io.github.jvlealc.securecapita.domain.Role;
import io.github.jvlealc.securecapita.dto.RoleDto;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public RoleDto toDto(Role role) {
        return new RoleDto(role.getName(), role.getPermission());
    }
}
