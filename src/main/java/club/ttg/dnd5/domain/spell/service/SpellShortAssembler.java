package club.ttg.dnd5.domain.spell.service;

import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellClassAffiliationRow;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellAffiliationDto;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Собирает краткие ответы о заклинаниях для списков.
 *
 * <p>Классы заклинаний читаются одним запросом на весь список и только нужными полями.
 * Через ленивую связь {@code Spell.classAffiliation} на каждое заклинание уходил отдельный
 * запрос, поднимавший классы целиком: список раздела «Заклинания» (2400 записей) собирался
 * почти две секунды.</p>
 */
@Component
@RequiredArgsConstructor
public class SpellShortAssembler
{
    private final SpellRepository spellRepository;
    private final SpellMapper spellMapper;

    /**
     * Краткие ответы в порядке исходного списка.
     *
     * @param spells заклинания списка.
     * @return по ответу на каждое заклинание.
     */
    public List<SpellShortResponse> toShort(final Collection<Spell> spells)
    {
        if (CollectionUtils.isEmpty(spells))
        {
            return List.of();
        }

        Map<String, Set<SpellAffiliationDto>> classesBySpellUrl = classesBySpellUrl(spells);

        return spells.stream()
                .map(spell -> toShort(spell, classesBySpellUrl))
                .toList();
    }

    private SpellShortResponse toShort(final Spell spell,
                                       final Map<String, Set<SpellAffiliationDto>> classesBySpellUrl)
    {
        SpellShortResponse response = spellMapper.toShortWithoutClasses(spell);
        response.setClasses(classesBySpellUrl.getOrDefault(spell.getUrl(), Set.of()));

        return response;
    }

    /** Классы заклинаний по слагу заклинания, в том же порядке, что даёт маппер по сущности. */
    private Map<String, Set<SpellAffiliationDto>> classesBySpellUrl(final Collection<Spell> spells)
    {
        List<String> spellUrls = spells.stream()
                .map(Spell::getUrl)
                .distinct()
                .toList();

        Map<String, Set<SpellAffiliationDto>> classesBySpellUrl = new HashMap<>();

        for (SpellClassAffiliationRow row : spellRepository.findClassAffiliationRows(spellUrls))
        {
            classesBySpellUrl
                    .computeIfAbsent(row.spellUrl(), url -> new TreeSet<>(SpellAffiliationDto.BY_NAME_THEN_SOURCE))
                    .add(spellMapper.toAffiliationDto(row.classUrl(), row.className(), row.sourceAcronym()));
        }

        return classesBySpellUrl;
    }
}
