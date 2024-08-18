package club.ttg.dnd5.mapper.character;

import club.ttg.dnd5.dictionary.Dice;
import club.ttg.dnd5.dto.character.ClassRequest;
import club.ttg.dnd5.dto.character.ClassResponse;
import club.ttg.dnd5.model.character.ClassCharacter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(uses = ClassFeatureMapper.class)
public interface ClassMapper {
    ClassMapper MAPPER = Mappers.getMapper(ClassMapper.class);
    @Mapping(source = "name.rus", target = "name")
    @Mapping(source = "name.eng", target = "english")
    @Mapping(source = "name.alt", target = "alternative")
    @Mapping(source = "name.genitive", target = "genitive")
    @Mapping(source = "hitDice", target = "hitDice", qualifiedByName = "toDice")
    ClassCharacter toEntity(ClassRequest request);

    @Mapping(source = "name", target = "name.rus")
    @Mapping(source = "english", target = "name.eng")
    @Mapping(source = "alternative", target = "name.alt")
    @Mapping(source = "genitive", target = "name.genitive")
    @Mapping(source = "hitDice", target = "hitDice", qualifiedByName = "diceToSting")
    ClassResponse toResponse(ClassCharacter entity);

    @Named("toDice")
    default Dice toDice(final String dice) {
        return Dice.parse(dice);
    }

    @Named("diceToSting")
    default String diceToSting(final Dice dice) {
        return dice.getName();
    }
}
