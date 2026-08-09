package club.ttg.dnd5.domain.feat.rest.mapper;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.model.AbilityBonus;
import club.ttg.dnd5.domain.feat.rest.dto.FeatSelectResponse;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.rest.dto.FeatBackgroundDto;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
    @Mapping(source = "request.mechanics", target = "mechanics")
    @Mapping(source = "request.prerequisiteDetails", target = "prerequisiteDetails")
    @Mapping(target = "source", source = "source")
    Feat toEntity(FeatRequest request, Source source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "url", ignore = true)
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.original", target = "original")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.srdVersion", target = "srdVersion")
    @Mapping(source = "request.mechanics", target = "mechanics")
    @Mapping(source = "request.prerequisiteDetails", target = "prerequisiteDetails")
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

    /**
     * Держит плоское {@code abilities} в согласии с механикой: по этой колонке работает
     * публичный фильтр «Характеристика», а редактор с появлением
     * {@code mechanics.abilityBonuses} её больше не заполняет.
     *
     * <p>Пока механика у черты не заполнена — или заполнена так, что своего списка
     * характеристик в ней нет (повышение привязано к выбору через
     * {@code AbilityBonus.fromChoiceKey}), — значение из формы остаётся как есть: иначе
     * правка черты вычеркнула бы её из фильтра.</p>
     */
    @AfterMapping
    default void syncAbilitiesWithMechanics(@MappingTarget Feat feat) {
        List<Ability> flat = abilityBonuses(feat).stream()
                .map(AbilityBonus::getAbilities)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .toList();
        if (flat.isEmpty()) {
            return;
        }
        feat.setAbilities(flat);
    }

    /**
     * Сколько характеристик поднимает черта. Для «Улучшения характеристик» это 2:
     * вариант {@code +1 к двум} задаёт {@code count = 2}.
     */
    @Named("getAbilityScoreIncreaseOptions")
    default int getAbilityScoreIncreaseOptions(Feat feat) {
        List<AbilityBonus> bonuses = abilityBonuses(feat);
        if (!bonuses.isEmpty()) {
            return bonuses.stream()
                    .mapToInt(AbilityBonus::resolveCount)
                    .max()
                    .orElse(0);
        }
        return CollectionUtils.isEmpty(feat.getAbilities()) ? 0 : 1;
    }

    private static List<AbilityBonus> abilityBonuses(Feat feat) {
        if (feat.getMechanics() == null || CollectionUtils.isEmpty(feat.getMechanics().getAbilityBonuses())) {
            return List.of();
        }
        return List.copyOf(feat.getMechanics().getAbilityBonuses());
    }
}
