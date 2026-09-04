package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.common.model.mechanics.ClassSpellListGrant;
import club.ttg.dnd5.domain.common.model.mechanics.GrantedSpellRef;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListExpansion;
import club.ttg.dnd5.domain.common.model.mechanics.SpellListGroup;
import club.ttg.dnd5.domain.common.rest.dto.FeatSpellListGroupResponse;
import club.ttg.dnd5.domain.common.rest.dto.GrantedSpellResponse;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.domain.spell.rest.mapper.SpellMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
     * Выдаваемые записью заклинания с данными справочника — оба источника разом.
     *
     * <p>Источников у выдачи два: перечисленные ссылки ({@link SpellGrant#getSpells()}) и
     * списки классов целиком ({@link SpellGrant#getClassLists()}). Разбор один на обоих и
     * живёт здесь, а не у каждого раздела: черта, предыстория, класс и умение выдают
     * заклинания одной и той же механикой, и второй разбор разошёлся бы с первым на первой
     * же записи.</p>
     *
     * @param grant блок выдачи; {@code null} — запись заклинаний не выдаёт.
     * @return выданные заклинания; пустой список — выдавать нечего.
     */
    public List<GrantedSpellResponse> grantedSpells(final SpellGrant grant) {
        if (grant == null) {
            return List.of();
        }

        return grantedSpells(grant, shortSpellsByUrl(grantedSpellRefs(grant)),
                spellsByClassUrl(classSpellListUrls(grant)));
    }

    /**
     * Выдаваемые заклинания по уже найденным записям справочника — для вызывающих, которые
     * собрали заклинания нескольких записей одним запросом (умения класса и их варианты).
     *
     * <p>Порядок: сперва перечисленное — в порядке редактора, — затем списки классов, круг
     * за кругом. Одно и то же заклинание идёт один раз и с наименьшим уровнем открытия:
     * запись, где заклинание и перечислено с первого уровня, и попадает в список класса с
     * десятого, обязана отдать его с первого — иначе выдача оказалась бы слабее
     * написанного.</p>
     *
     * @param grant            блок выдачи; {@code null} — запись заклинаний не выдаёт.
     * @param spellsByUrl      найденные записи справочника по url.
     * @param spellsByClassUrl списки заклинаний по слагу класса.
     * @return выданные заклинания; пустой список — выдавать нечего.
     */
    public List<GrantedSpellResponse> grantedSpells(final SpellGrant grant,
                                                    final Map<String, SpellShortResponse> spellsByUrl,
                                                    final Map<String, List<SpellShortResponse>> spellsByClassUrl) {
        if (grant == null) {
            return List.of();
        }

        var byUrl = new LinkedHashMap<String, GrantedSpellResponse>();

        for (GrantedSpellRef ref : Optional.ofNullable(grant.getSpells()).orElse(List.of())) {
            if (ref == null) {
                continue;
            }
            var spell = spellsByUrl.get(ref.getUrl());
            if (spell != null) {
                put(byUrl, new GrantedSpellResponse(spell, ref.getRequiredLevel(), null,
                        ref.getSpellcastingAbility(), ref.getAlwaysPrepared()));
            }
        }

        for (ClassSpellListGrant classList : Optional.ofNullable(grant.getClassLists()).orElse(List.of())) {
            if (classList == null) {
                continue;
            }
            Boolean limitedBySlots = Boolean.TRUE.equals(classList.getMaxLevelFromSlots()) ? Boolean.TRUE : null;
            for (String classUrl : classUrls(classList)) {
                for (SpellShortResponse spell : spellsByClassUrl.getOrDefault(classUrl, List.of())) {
                    if (matchesLevel(classList, spell)) {
                        put(byUrl, new GrantedSpellResponse(spell, classList.getRequiredLevel(), limitedBySlots,
                                classList.getSpellcastingAbility(), classList.getAlwaysPrepared()));
                    }
                }
            }
        }

        return List.copyOf(byUrl.values());
    }

    /**
     * Перечисленные ссылки блока — чтобы вызывающий собрал один запрос на несколько
     * записей вместо запроса на каждую.
     *
     * @param grant блок выдачи; {@code null} — выдавать нечего.
     * @return ссылки на заклинания; пусто — блок ничего не перечисляет.
     */
    public List<GrantedSpellRef> grantedSpellRefs(final SpellGrant grant) {
        return grant == null ? List.of()
                : Optional.ofNullable(grant.getSpells()).orElse(List.<GrantedSpellRef>of()).stream()
                        .filter(Objects::nonNull)
                        .toList();
    }

    /**
     * Слаги классов, чьи списки блок выдаёт целиком, — по той же причине, что и
     * {@link #grantedSpellRefs(SpellGrant)}: один запрос на весь класс, а не на умение.
     *
     * @param grant блок выдачи; {@code null} — выдавать нечего.
     * @return слаги классов без повторов; пусто — списков классов в блоке нет.
     */
    public List<String> classSpellListUrls(final SpellGrant grant) {
        return grant == null ? List.of()
                : Optional.ofNullable(grant.getClassLists()).orElse(List.<ClassSpellListGrant>of()).stream()
                        .filter(Objects::nonNull)
                        .map(GrantedSpellResolver::classUrls)
                        .flatMap(List::stream)
                        .distinct()
                        .toList();
    }

    /**
     * Списки заклинаний классов — одним запросом на все названные классы.
     *
     * <p>Заклинание, доступное двум классам, приходит в оба списка: карта строится по
     * принадлежности записи, а не делит справочник между классами.</p>
     *
     * @param classUrls слаги классов.
     * @return заклинания по слагу класса; пустая карта — искать было нечего.
     */
    public Map<String, List<SpellShortResponse>> spellsByClassUrl(final Collection<String> classUrls) {
        if (CollectionUtils.isEmpty(classUrls)) {
            return Map.of();
        }

        var wanted = Set.copyOf(classUrls);
        var result = new LinkedHashMap<String, List<SpellShortResponse>>();
        var seen = new HashSet<String>();

        for (Spell spell : spellRepository.findAllShortByClassUrlIn(wanted)) {
            // Соединение с таблицей принадлежности может отдать запись дважды: заклинание
            // доступно двум названным классам, а строки соединения — две
            if (!seen.add(spell.getUrl())) {
                continue;
            }
            var response = spellMapper.toShort(spell);
            for (CharacterClass affiliation : Optional.ofNullable(spell.getClassAffiliation())
                    .orElse(Set.of())) {
                if (affiliation != null && wanted.contains(affiliation.getUrl())) {
                    result.computeIfAbsent(affiliation.getUrl(), url -> new ArrayList<>()).add(response);
                }
            }
        }

        return result;
    }

    /** Слаги классов группы без пустых и повторов. */
    private static List<String> classUrls(final ClassSpellListGrant classList) {
        return Optional.ofNullable(classList.getClasses()).orElse(List.<EntityRef>of()).stream()
                .filter(Objects::nonNull)
                .map(EntityRef::getUrl)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    /**
     * Подходит ли круг заклинания под ограничение группы.
     *
     * <p>Отметка «по ячейкам» здесь не режет ничего: круга персонажа сервер не знает, и
     * список уезжает целиком с меткой {@code limitedBySlots}.</p>
     */
    private static boolean matchesLevel(final ClassSpellListGrant classList, final SpellShortResponse spell) {
        Long level = spell.getLevel();

        if (level == null) {
            return true;
        }
        if (classList.getLevel() != null) {
            return level.longValue() == classList.getLevel().longValue();
        }
        if (classList.getMaxLevel() != null) {
            return level <= classList.getMaxLevel().longValue();
        }
        return true;
    }

    /** Кладёт заклинание, оставляя за уже лежащим наименьший уровень открытия. */
    private static void put(final Map<String, GrantedSpellResponse> byUrl, final GrantedSpellResponse granted) {
        var url = granted.getSpell().getUrl();
        var existing = byUrl.get(url);

        if (existing == null || isEarlier(granted.getRequiredLevel(), existing.getRequiredLevel())) {
            byUrl.put(url, granted);
        }
    }

    /** Раньше ли открывается уровень {@code candidate}; пустой уровень — «сразу». */
    private static boolean isEarlier(final Integer candidate, final Integer current) {
        if (current == null) {
            return false;
        }
        return candidate == null || candidate < current;
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
