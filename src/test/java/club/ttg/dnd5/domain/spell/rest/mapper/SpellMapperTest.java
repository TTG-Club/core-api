package club.ttg.dnd5.domain.spell.rest.mapper;

import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.model.SpellSchool;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SpellMapperTest
{
    @Mock
    private SpellComponentsMapper spellComponentsMapper;

    @Mock
    private BaseMapping baseMapping;

    @InjectMocks
    private SpellMapperImpl mapper;

    /** Разбор альтернативных названий — настоящий: проверяется именно пара «склейка + разбор». */
    private final BaseMapping reading = new BaseMapping() {};

    @Test
    void alternativeNamesSurviveSaveWithoutGrowingSpaces()
    {
        List<String> original = List.of("Иной облик", "Изменить себя");

        String stored = mapper.joinAlternative(original);
        List<String> readBack = List.copyOf(reading.altToCollection(stored));

        assertEquals(original, readBack);
        assertEquals(stored, mapper.joinAlternative(readBack));
    }

    @Test
    void alternativeNamesLoseAccumulatedSpacesOnRead()
    {
        assertEquals(List.of("Иной облик", "Изменить себя"),
                List.copyOf(reading.altToCollection("Иной облик;        Изменить себя")));
    }

    @Test
    void shortResponseKeepsSchoolClarificationSeparate()
    {
        Spell spell = new Spell();
        spell.setSchool(SpellSchool.builder()
                .school(MagicSchool.EVOCATION)
                .additionalType("песнь")
                .build());

        var response = mapper.toShort(spell);

        assertEquals(MagicSchool.EVOCATION.getName(), response.getSchool());
        assertEquals("песнь", response.getAdditionalType());
    }
}
