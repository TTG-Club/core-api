package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.section.CreatureSection;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.CreatureSectionDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.CretureSectionRequest;
import club.ttg.dnd5.domain.common.dictionary.Habitat;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.CreatureSectionShortResponse;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collection;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CreatureMapper.class, BaseMapping.class})
public interface CreatureSectionMapper {
    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    CreatureSectionShortResponse toShort(CreatureSection section);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "habitats", target = "habitats", qualifiedByName = "toHabitats")
    CreatureSectionDetailResponse toDetail(CreatureSection section);

    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    CretureSectionRequest toRequest(CreatureSection section);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    CreatureSection toEntity(CretureSectionRequest request, Book source);

    @Named("toHabitats")
    default String toHabitats(Collection<Habitat> habitats) {
        return habitats
                .stream()
                .map(Habitat::getName)
                .collect(Collectors.joining(", "));
    }
}
