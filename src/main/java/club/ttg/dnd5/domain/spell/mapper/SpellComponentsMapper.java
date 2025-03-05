package club.ttg.dnd5.domain.spell.mapper;

import club.ttg.dnd5.domain.spell.model.MaterialComponent;
import club.ttg.dnd5.domain.spell.model.SpellComponents;
import club.ttg.dnd5.domain.spell.rest.dto.SpellDetailedComponents;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortComponents;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface SpellComponentsMapper {
    @Mapping(target = "m", source = "m", qualifiedByName = "stringNotEmpty")
    SpellShortComponents toSpellShortComponents(SpellComponents components);

    @Mapping(target = "m", source = "m.text")
    SpellDetailedComponents toSpellDetailedComponents(SpellComponents components);

    @Named("stringNotEmpty")
    default Boolean stringNotEmpty(MaterialComponent material) {
        return Optional.ofNullable(material)
                .map(MaterialComponent::getText)
                .map(StringUtils::isNotEmpty)
                .orElse(false);
    }
}
