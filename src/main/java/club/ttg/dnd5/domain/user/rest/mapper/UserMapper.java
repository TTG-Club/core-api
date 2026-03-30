package club.ttg.dnd5.domain.user.rest.mapper;

import club.ttg.dnd5.domain.user.model.Role;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.rest.dto.UserDto;
import club.ttg.dnd5.domain.user.rest.dto.UserProfileShortResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserProfileShortResponse toProfileShortResponse(User user);

    @Mapping(source = "uuid", target = "id")
    @Mapping(source = "roles", target = "roles", qualifiedByName = "toRoleList")
    UserDto toDto(User user);

    @Named("toRoleList")
    default List<String> toRoleList(List<Role> roles) {
        return roles.stream().map(Role::getName).toList();
    }
}
