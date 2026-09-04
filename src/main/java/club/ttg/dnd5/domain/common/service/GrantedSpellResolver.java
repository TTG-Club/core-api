package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListExpansion;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListGroup;
import club.ttg.dnd5.domain.common.rest.dto.FeatSpellListGroupResponse;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    /**
     * Ссылки всех списков блока расширения — чтобы вызывающий собрал один запрос на
     * несколько записей (умения класса и их варианты) вместо запроса на каждую.
     *
     * @param expansion блок расширения; {@code null} — запись список не расширяет.
     * @return ссылки на заклинания всех списков; пусто — расширять нечем.
     */
    public List<EntityRef> spellListRefs(final SpellListExpansion expansion) {
        return resolveGroups(expansion).stream()
                .map(SpellListGroup::getSpells)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Списки расширения с данными справочника — одним запросом на блок.
     *
     * <p>Расширение — доступность, а не выдача: лист персонажа показывает эти заклинания в
     * окне добавления рядом с классовыми, и без круга ему их некуда положить. Список без
     * единого найденного заклинания выбрасывается целиком: пустая ступень на странице
     * читалась бы как «на этом уровне ничего не открывается», хотя на деле там опечатка
     * в url.</p>
     *
     * @param expansion блок расширения; {@code null} — запись список не расширяет.
     * @return списки с данными справочника; {@code null} — расширять нечем.
     */
    public Collection<FeatSpellListGroupResponse> spellListGroups(final SpellListExpansion expansion) {
        var refs = spellListRefs(expansion);

        return refs.isEmpty() ? null : spellListGroups(expansion, shortSpellsByUrl(refs));
    }

    /**
     * Списки расширения по уже найденным записям справочника — для вызывающих, которые
     * собрали заклинания нескольких записей одним запросом.
     *
     * @param expansion   блок расширения; {@code null} — запись список не расширяет.
     * @param spellsByUrl найденные записи справочника по url.
     * @return списки с данными справочника; {@code null} — расширять нечем.
     */
    public Collection<FeatSpellListGroupResponse> spellListGroups(final SpellListExpansion expansion,
                                                                   final Map<String, SpellShortResponse> spellsByUrl) {
        var result = resolveGroups(expansion).stream()
                .map(group -> {
                    var spells = Optional.ofNullable(group.getSpells()).orElse(List.<EntityRef>of()).stream()
                            .filter(Objects::nonNull)
                            .map(ref -> spellsByUrl.get(ref.getUrl()))
                            .filter(Objects::nonNull)
                            .toList();
                    return spells.isEmpty() ? null : new FeatSpellListGroupResponse(
                            group.getRequiredLevel(), group.getCount(), spells);
                })
                .filter(Objects::nonNull)
                .toList();

        return result.isEmpty() ? null : result;
    }

    /** Списки блока с поправкой на прежнюю плоскую форму — см. {@link SpellListExpansion#resolveGroups()}. */
    private static List<SpellListGroup> resolveGroups(final SpellListExpansion expansion) {
        return Optional.ofNullable(expansion)
                .map(SpellListExpansion::resolveGroups)
                .orElse(List.of());
    }
}
