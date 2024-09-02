package club.ttg.dnd5.mapper.species;

import club.ttg.dnd5.dto.base.SourceResponse;
import club.ttg.dnd5.model.Source;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SourceMapper {
    SourceMapper INSTANCE = Mappers.getMapper(SourceMapper.class);

    @Mapping(source = "source", target = "source")
    SourceResponse toSourceResponse(Source source);

    @Mapping(source = "source", target = "source")
    Source toSource(SourceResponse sourceResponse);
}
