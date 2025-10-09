package club.ttg.dnd5.domain.user.rest.mapper;

import club.ttg.dnd5.domain.user.model.Role;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.rest.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "roles", target = "roles", qualifiedByName = "toRoles")
    UserDto toDto(User user);

    @Named("toRoles")
    default List<String> toRoles(List<Role> roles) {
        return roles.stream().map(Role::getName).toList();
    }
}
