package club.ttg.dnd5.domain.user.rest.mapper;

import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.rest.dto.UserProfileShortResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    UserProfileShortResponse toProfileShortResponse(User user);
}
