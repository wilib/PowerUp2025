package co.com.pragma.r2dbc.mapper;

import co.com.pragma.model.role.Role;
import co.com.pragma.model.user.User;
import co.com.pragma.r2dbc.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    @Mapping(target = "roleId", source = "role", qualifiedByName = "roleToId")
    UserEntity toEntity(User user);

    @Mapping(target = "role", source = "roleId", qualifiedByName = "idToRole")
    User toDomain(UserEntity entity);

    @Named("roleToId")
    default String roleToId(Role role) {
        return role == null ? null : role.getRoleId();
    }

    @Named("idToRole")
    default Role idToRole(String roleId) {
        return roleId == null ? null : new Role(roleId, null);
    }
}
