package club.ttg.dnd5.domain.glossary.rest.mapper;

import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryRequest;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GlossaryMapper {

    GlossaryResponse toGlossaryDetailedResponse(Glossary glossary);

    GlossaryRequest toGlossaryRequest(Glossary glossary);

    Glossary toEntity(GlossaryRequest request);

    @Mapping(target = "url", source = "url")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "english", source = "english")
    @Mapping(target = "alternative", source = "alternative")
    @Mapping(target = "tags", source = "tags")
    GlossaryResponse toResponse(Glossary glossary);

    @Mapping(target = "url", ignore = true)
    void updateEntity(@MappingTarget Glossary glossary, GlossaryRequest request);
}
