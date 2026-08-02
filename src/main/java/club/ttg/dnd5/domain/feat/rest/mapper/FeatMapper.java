package club.ttg.dnd5.domain.feat.rest.mapper;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.feat.rest.dto.FeatSelectResponse;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.rest.dto.FeatBackgroundDto;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.util.StringUtils;

import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {BaseMapping.class})
public interface FeatMapper {

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = "category.name", target = "category", qualifiedByName = "capitalize")
    FeatShortResponse toShort(Feat feat);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = "category.name", target = "category", qualifiedByName = "capitalize")
    FeatDetailResponse toDetail(Feat feat);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.original", target = "original")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.srdVersion", target = "srdVersion")
    @Mapping(target = "source", source = "source")
    Feat toEntity(FeatRequest request, Source source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "url", ignore = true)
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.original", target = "original")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.srdVersion", target = "srdVersion")
    @Mapping(target = "source", source = "source")
    void updateEntity(FeatRequest request, Source source, @MappingTarget Feat feat);

    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    FeatRequest toRequest(Feat feat);

    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceMapping
    @Mapping(source = ".", target = "abilityScoreIncreaseOptions", qualifiedByName = "getAbilityScoreIncreaseOptions")
    FeatSelectResponse toSelect(Feat feat);

    /**
     * Ссылка на предысторию для детальника черты. Аббревиатура источника добавляется к названию,
     * чтобы различать одноимённые предыстории из разных книг — как в блоках привязок заклинания.
     */
    default FeatBackgroundDto toBackgroundDto(Background background) {
        if (background == null) {
            return null;
        }

        String name = background.getName();

        if (background.getSource() != null && StringUtils.hasText(background.getSource().getAcronym())) {
            name = name + " [" + background.getSource().getAcronym() + "]";
        }

        return new FeatBackgroundDto(background.getUrl(), name);
    }

    @Named("capitalize")
    default String capitalize(String string) {
        return StringUtils.capitalize(string);
    }

    @Named("getAbilityScoreIncreaseOptions")
    default int getAbilityScoreIncreaseOptions(Feat feat) {
        if (feat.getName().equals("Улучшение характеристик")) {
            return 2;
        }
        if (feat.getAbilities() == null || feat.getAbilities().isEmpty()) {{
            return 0;
        }}
        return 1;
    }
}
