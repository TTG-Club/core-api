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
