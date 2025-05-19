package club.ttg.dnd5.domain.beastiary.rest.mapper;

import club.ttg.dnd5.domain.beastiary.model.section.BeastSection;
import club.ttg.dnd5.domain.beastiary.model.section.Habitat;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.BeastSectionDetailResponse;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.BeastSectionRequest;
import club.ttg.dnd5.domain.beastiary.rest.dto.section.BeastSectionShortResponse;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collection;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {BeastMapper.class, BaseMapping.class})
public interface BeastSectionMapper {
    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    BeastSectionShortResponse toShort(BeastSection section);

    @BaseMapping.BaseSourceMapping
    @BaseMapping.BaseShortResponseNameMapping
    @Mapping(source = "habitats", target = "habitats", qualifiedByName = "toHabitats")
    BeastSectionDetailResponse toDetail(BeastSection section);

    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    BeastSectionRequest toRequest(BeastSection section);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(target = "source", source = "source")
    BeastSection toEntity(BeastSectionRequest request, Book source);

    @Named("toHabitats")
    default String toHabitats(Collection<Habitat> habitats) {
        return habitats
                .stream()
                .map(Habitat::getName)
                .collect(Collectors.joining(", "));
    }
}
