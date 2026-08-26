package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Записи справочника заклинаний по ссылкам из механики.
 *
 * <p>Механика хранит выдаваемые заклинания ссылками ({@link EntityRef}), а потребителю
 * нужны круг и школа: лист персонажа без круга не положит заклинание в книгу. Достаёт их
 * и черта, и предыстория — механика у них одна и та же модель, поэтому и разбор один.</p>
 */
@Component
@RequiredArgsConstructor
public class GrantedSpellResolver {
    private final SpellRepository spellRepository;
    private final SpellMapper spellMapper;

    /**
     * Достаёт записи справочника по ссылкам механики — одним запросом на список.
     *
     * <p>Ненайденное заклинание в карту просто не попадёт, и вызывающий его пропустит.
     * Ронять страницу целиком из-за опечатки в url нельзя: это свободный JSONB, набранный
     * руками в редакторе, а не запись связующей таблицы.</p>
     *
     * @param refs ссылки на заклинания из механики.
     * @return найденные записи справочника по url; пустая карта — искать было нечего.
     */
    public Map<String, SpellShortResponse> shortSpellsByUrl(final Collection<? extends EntityRef> refs) {
        var urls = refs.stream()
                .filter(Objects::nonNull)
                .map(EntityRef::getUrl)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (urls.isEmpty()) {
            return Map.of();
        }

        return spellRepository.findAllShortByUrlIn(urls)
                .stream()
                .collect(Collectors.toMap(Spell::getUrl, spellMapper::toShort));
    }
}
