package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellClassAffiliationRow;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellAffiliationDto;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Answers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpellShortAssemblerTest
{
    private final SpellRepository spellRepository = mock(SpellRepository.class);
    private final SpellMapper spellMapper = mock(SpellMapper.class, Answers.CALLS_REAL_METHODS);
    private final SpellShortAssembler assembler = new SpellShortAssembler(spellRepository, spellMapper);

    @Test
    void classesComeFromRowsInMapperOrderAndListKeepsItsOrder()
    {
        Spell fireball = spell("fireball-phb");
        Spell light = spell("light-phb");
        Spell wish = spell("wish-phb");

        when(spellMapper.toShortWithoutClasses(any())).thenAnswer(call -> {
            SpellShortResponse response = new SpellShortResponse();
            response.setUrl(call.<Spell>getArgument(0).getUrl());
            return response;
        });
        when(spellRepository.findClassAffiliationRows(List.of("fireball-phb", "light-phb", "wish-phb")))
                .thenReturn(List.of(
                        new SpellClassAffiliationRow("fireball-phb", "wizard-phb", "Волшебник", "PHB"),
                        new SpellClassAffiliationRow("fireball-phb", "sorcerer-phb", "Чародей", "PHB"),
                        new SpellClassAffiliationRow("light-phb", "bard-phb", "Бард", null),
                        new SpellClassAffiliationRow("light-phb", "artificer-efa", "Изобретатель", " ")));

        List<SpellShortResponse> responses = assembler.toShort(List.of(fireball, light, wish));

        assertEquals(List.of("fireball-phb", "light-phb", "wish-phb"),
                responses.stream().map(SpellShortResponse::getUrl).toList());
        // Порядок — по подписи, как у маппера по сущности; акроним дописывается в подпись
        assertEquals(List.of("Волшебник [PHB]", "Чародей [PHB]"), names(responses.get(0)));
        // Пустой акроним подпись не меняет
        assertEquals(List.of("Бард", "Изобретатель"), names(responses.get(1)));
        assertTrue(responses.get(2).getClasses().isEmpty());
    }

    @Test
    void emptyListDoesNotQueryClasses()
    {
        assertTrue(assembler.toShort(List.of()).isEmpty());

        verify(spellRepository, never()).findClassAffiliationRows(any());
    }

    private static List<String> names(final SpellShortResponse response)
    {
        return response.getClasses().stream().map(SpellAffiliationDto::getName).toList();
    }

    private static Spell spell(final String url)
    {
        Spell spell = new Spell();
        spell.setUrl(url);
        return spell;
    }
}
