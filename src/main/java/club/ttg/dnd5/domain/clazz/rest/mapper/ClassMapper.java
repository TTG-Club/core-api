package club.ttg.dnd5.domain.clazz.rest.mapper;

import club.ttg.dnd5.domain.clazz.model.ClassCharacter;
import club.ttg.dnd5.domain.clazz.rest.dto.ClassDetailResponse;
import club.ttg.dnd5.domain.clazz.rest.dto.ClassRequest;
import club.ttg.dnd5.domain.clazz.rest.dto.ClassShortResponse;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = ClassFeatureMapper.class)
public interface ClassMapper {
    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "hitDice", target = "hitDice", qualifiedByName = "diceToString")
    @Mapping(source = "source.name", target = "source.group.name")
    @Mapping(source = "source.sourceAcronym", target = "source.name.name")
    ClassShortResponse toShortDto(ClassCharacter entity);

    @Mapping(source = "name", target = "name.name")
    @Mapping(source = "english", target = "name.english")
    @Mapping(source = "mainAbility", target = "mainAbility", qualifiedByName = "abilityToString")
    @Mapping(source = "hitDice", target = "hitDice", qualifiedByName = "diceToString")
    @Mapping(source = "source.name", target = "source.group.name")
    @Mapping(source = "source.sourceAcronym", target = "source.name.name")
    ClassDetailResponse toDetailDto(ClassCharacter entity);

    @Mapping(source = "name.name", target = "name")
    @Mapping(source = "name.english", target = "english")
    @Mapping(source = "genitive", target = "genitive")
    @Mapping(source = "hitDice", target = "hitDice", qualifiedByName = "toDice")
    @Mapping(source = "name.alternative", target = "alternative", qualifiedByName = "collectToString")
    ClassCharacter toEntity(ClassRequest request);

    @Named("toDice")
    default Dice toDice(final String dice) {
        return Dice.parse(dice);
    }

    @Named("abilityToString")
    default String abilityToString(Set<Ability> abilities) {
        return abilities.stream()
                .map(Ability::getName)
                .collect(Collectors.joining(" и "));
    }

    @Named("diceToString")
    default String diceToString(final Dice dice) {
        return dice.getName();
    }

    @Named("collectToString")
    default String collectToString(Collection<String> names) {
        return String.join(" ", names);
    }
}
