package club.ttg.dnd5.domain.token.rest.mapper;

import club.ttg.dnd5.domain.token.model.TokenBorder;
import club.ttg.dnd5.domain.token.rest.dto.TokenBorderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TokenBorderMapper {
    @Mapping(source = "id", target = "id", qualifiedByName = "toId")
    TokenBorderResponse toResponse(TokenBorder tokenBorder);

    @Named("toId")
    default String toId(UUID id) {
        return  id.toString();
    }
}
