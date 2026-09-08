package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellGroup;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellRef;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellRestKind;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellUsageMode;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellcastingBlock;
import club.ttg.dnd5.domain.beastiary.model.spellcasting.CreatureSpellComponents;
import club.ttg.dnd5.domain.common.dictionary.RechargeType;
import club.ttg.dnd5.domain.spell.model.Spell;
import club.ttg.dnd5.domain.spell.repository.SpellRepository;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpell;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgSpellUses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Заклинания существа блоками → формат компендиума VTTG.
 *
 * <p>Выгрузка распадается натрое, потому что натрое её делит сама система. Сами
 * заклинания уезжают готовыми записями справочника в {@code VttgCreature.spells} — их
 * система читает как {@code DnDCreature.spells}. Раскладка по блокам и группам уезжает
 * в {@code system.spellcastingBlocks} — тем же ключом, каким её пишет мир, когда блоки
 * заводят руками на листе существа: одна и та же запись не должна выглядеть по-разному
 * в зависимости от того, откуда пришла.</p>
 *
 * <p>Третьим идёт {@code system.spellcasting} — три плоских числа на всё существо
 * ({@code ability}, {@code saveDC}, {@code attackBonus}) по первому блоку, где они
 * заданы. Это запасные числа: блок берёт их, когда своих не имеет.</p>
 *
 * <p>Заклинания резолвятся одним запросом на существо: мапперы существ зовут пачками
 * ({@code VttgChangesService}, {@code VttgModuleService}), и запрос на заклинание там
 * обернулся бы сотнями походов в базу.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgCreatureSpellcastingMapper {

    private final SpellRepository spellRepository;
    private final VttgSpellMapper spellMapper;

    /**
     * Заклинательство существа в трёх местах выгрузки.
     *
     * @param spells       заклинания для {@code VttgCreature.spells}; пусто — выгружать нечего.
     * @param spellcasting значение ключа {@code system.spellcasting} — запасные числа существа;
     *                     {@code null} — ни у одного блока их нет и ключ не кладётся.
     * @param blocks       значение ключа {@code system.spellcastingBlocks}; {@code null} — блоков
     *                     нет и ключ не кладётся вовсе.
     */
    public record Exported(List<VttgSpell> spells,
                           Map<String, Object> spellcasting,
                           List<Map<String, Object>> blocks) {
        static final Exported EMPTY = new Exported(List.of(), null, null);
    }

    /**
     * Собирает заклинательство существа.
     *
     * @param blocks блоки заклинаний существа.
     * @return заклинания и параметры блоков; {@link Exported#EMPTY}, если блоков нет.
     */
    public Exported export(List<CreatureSpellcastingBlock> blocks) {
        List<CreatureSpellcastingBlock> filled = blocks == null ? List.of() : blocks.stream()
                .filter(Objects::nonNull)
                .toList();
        if (filled.isEmpty()) {
            return Exported.EMPTY;
        }
        return new Exported(spells(filled), spellcasting(filled), blocks(filled));
    }

    /**
     * Заклинания блоков готовыми записями справочника.
     *
     * <p>Поверх справочной записи ставятся заряды порции и заклинательная характеристика
     * блока. Плоский бонус атаки блока сюда не идёт: {@code VttgSpell.attackBonus} — это
     * прибавка СВЕРХ характеристики, а у существа число готовое, и сложение испортило бы
     * бросок. Бонус остаётся в {@code system.spellcasting}.</p>
     *
     * <p>Заклинание, которого в справочнике нет (карточку удалили), пропускается: собирать
     * запись не из чего. В блоках его слаг остаётся — по нему видно, что у существа было.</p>
     */
    private List<VttgSpell> spells(List<CreatureSpellcastingBlock> blocks) {
        Set<String> urls = blocks.stream()
                .flatMap(block -> groups(block).stream())
                .flatMap(group -> spellRefs(group).stream())
                .map(CreatureSpellRef::getUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (urls.isEmpty()) {
            return List.of();
        }

        Map<String, Spell> catalog = spellRepository.findAllForVttgExportByUrls(urls).stream()
                .filter(spell -> StringUtils.hasText(spell.getUrl()))
                .collect(Collectors.toMap(Spell::getUrl, Function.identity(), (first, second) -> first));

        Set<String> exported = new LinkedHashSet<>();
        List<VttgSpell> result = new ArrayList<>();
        for (CreatureSpellcastingBlock block : blocks) {
            String ability = VttgDictionaries.ability(block.getAbility());
            for (CreatureSpellGroup group : groups(block)) {
                VttgSpellUses uses = uses(group);
                for (CreatureSpellRef ref : spellRefs(group)) {
                    String url = ref.getUrl();
                    Spell spell = StringUtils.hasText(url) ? catalog.get(url) : null;
                    // Первая порция, где заклинание встретилось, и решает его заряды:
                    // на листе существа заклинание одно, а счётчик у него один.
                    if (spell == null || !exported.add(url)) {
                        continue;
                    }
                    VttgSpell base = spellMapper.toVttg(spell);
                    result.add(base.toBuilder()
                            .uses(uses)
                            .spellcastingAbility(ability == null ? base.getSpellcastingAbility() : ability)
                            .build());
                }
            }
        }
        return result;
    }

    /**
     * Заряды заклинания по режиму порции.
     *
     * <p>{@code SpellUses} в VTTG — счётчик ОДНОГО заклинания, поэтому общий пул порции
     * («1/день» на весь список) и перезарядка ей невыразимы: у таких порций зарядов на
     * заклинании нет вовсе. Пусть лучше счётчика не будет, чем он будет неверным —
     * ограничение видно в {@code system.spellcastingBlocks}.</p>
     *
     * @param group порция блока.
     * @return заряды заклинания или {@code null}, если счётчик порции невыразим.
     */
    private VttgSpellUses uses(CreatureSpellGroup group) {
        if (group.getMode() == null) {
            return null;
        }
        return switch (group.getMode()) {
            // «Постоянно активно» тратить нечего — тот же неограниченный счётчик, что и
            // «по желанию»: другого способа сказать это у листа нет.
            case AT_WILL, CONSTANT -> VttgSpellUses.builder()
                    .max(0)
                    .current(0)
                    .recovery("atWill")
                    .build();
            case PER_DAY_EACH -> counted(group.getCount(), "longRest");
            case PER_REST_EACH -> counted(group.getCount(),
                    group.getRest() == CreatureSpellRestKind.SHORT ? "shortRest" : "longRest");
            case PER_DAY_POOL, PER_REST_POOL, RECHARGE -> null;
        };
    }

    /** Счётчик порции; без внятного числа применений заряды не ставятся. */
    private VttgSpellUses counted(Integer count, String recovery) {
        if (count == null || count < 1) {
            return null;
        }
        return VttgSpellUses.builder()
                .max(count)
                .current(count)
                .recovery(recovery)
                .build();
    }

    /**
     * Ключ {@code system.spellcasting}: запасные числа существа.
     *
     * <p>Берутся из первого блока, где поле задано, и в дело идут только у блока без
     * своих чисел. Пусто у всех блоков — ключа нет вовсе: пустая настройка выглядела бы
     * как заданная и перебила бы расчёт по характеристике.</p>
     */
    private Map<String, Object> spellcasting(List<CreatureSpellcastingBlock> blocks) {
        Map<String, Object> result = new LinkedHashMap<>();
        putIfNotNull(result, "ability", VttgDictionaries.ability(
                first(blocks, CreatureSpellcastingBlock::getAbility)));
        putIfNotNull(result, "saveDC", first(blocks, CreatureSpellcastingBlock::getSaveDc));
        putIfNotNull(result, "attackBonus", first(blocks, CreatureSpellcastingBlock::getAttackBonus));
        return result.isEmpty() ? null : result;
    }

    /** Ключ {@code system.spellcastingBlocks}: раскладка заклинаний по блокам и группам. */
    private List<Map<String, Object>> blocks(List<CreatureSpellcastingBlock> blocks) {
        return blocks.stream().map(this::block).toList();
    }

    /**
     * Блок в формате компендиума.
     *
     * <p>{@code id} обязателен: при переименовании блока это единственное, за что система
     * может за него держаться. Генерирует его форма — здесь он только переписывается.</p>
     */
    private Map<String, Object> block(CreatureSpellcastingBlock block) {
        Map<String, Object> result = new LinkedHashMap<>();
        putIfHasText(result, "id", block.getId());
        putIfHasText(result, "name", block.getName());
        putIfHasText(result, "note", block.getNote());
        putIfNotNull(result, "ability", VttgDictionaries.ability(block.getAbility()));
        putIfNotNull(result, "saveDC", block.getSaveDc());
        putIfNotNull(result, "attackBonus", block.getAttackBonus());
        putIfNotNull(result, "ignoredComponents",
                ignoredComponents(block.getIgnoredComponents()));
        result.put("groups", groups(block).stream().map(this::group).toList());
        return result;
    }

    /**
     * Порция в формате компендиума. Заклинания идут записями {@code {id, castLevel, note}}, а
     * не голыми слагами: круг наложения и оговорка статблока принадлежат паре
     * «заклинание в этой порции», и в самой записи заклинания места им нет.
     */
    private Map<String, Object> group(CreatureSpellGroup group) {
        Map<String, Object> result = new LinkedHashMap<>();
        putIfNotNull(result, "mode", mode(group.getMode()));
        putIfNotNull(result, "count", group.getCount());
        putIfNotNull(result, "rest", rest(group.getRest()));
        putIfNotNull(result, "recharge", recharge(group.getRecharge()));
        putIfHasText(result, "label", group.getLabel());
        result.put("spells", spellRefs(group).stream()
                .filter(ref -> StringUtils.hasText(ref.getUrl()))
                .map(this::spellRef)
                .toList());
        return result;
    }

    private Map<String, Object> spellRef(CreatureSpellRef ref) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", ref.getUrl());
        putIfNotNull(result, "castLevel", ref.getCastLevel());
        putIfHasText(result, "note", ref.getNote());
        return result;
    }

    /**
     * Компоненты, которые блоку не требуются, — тремя отметками, как в форме бестиария и
     * в записи системы. Ни одной отметки — ключа нет: блоку нужны все компоненты.
     */
    private Map<String, Object> ignoredComponents(CreatureSpellComponents components) {
        if (components == null
                || !(components.isVerbal() || components.isSomatic() || components.isMaterial())) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("verbal", components.isVerbal());
        result.put("somatic", components.isSomatic());
        result.put("material", components.isMaterial());
        return result;
    }

    /** Режим порции в вокабуляре VTTG: {@code PER_DAY_EACH → perDayEach}. */
    private String mode(CreatureSpellUsageMode mode) {
        if (mode == null) {
            return null;
        }
        return switch (mode) {
            case AT_WILL -> "atWill";
            case PER_DAY_EACH -> "perDayEach";
            case PER_DAY_POOL -> "perDayPool";
            case PER_REST_EACH -> "perRestEach";
            case PER_REST_POOL -> "perRestPool";
            case RECHARGE -> "recharge";
            case CONSTANT -> "constant";
        };
    }

    private String rest(CreatureSpellRestKind rest) {
        return rest == null ? null : rest.name().toLowerCase(Locale.ROOT);
    }

    /** Перезарядка порции тем же ключом, что у записи существа: {@code D5 → d5}. */
    private String recharge(RechargeType recharge) {
        return recharge == null ? null : recharge.name().toLowerCase(Locale.ROOT);
    }

    private List<CreatureSpellGroup> groups(CreatureSpellcastingBlock block) {
        if (CollectionUtils.isEmpty(block.getGroups())) {
            return List.of();
        }
        return block.getGroups().stream().filter(Objects::nonNull).toList();
    }

    private List<CreatureSpellRef> spellRefs(CreatureSpellGroup group) {
        if (CollectionUtils.isEmpty(group.getSpells())) {
            return List.of();
        }
        return group.getSpells().stream().filter(Objects::nonNull).toList();
    }

    /** Первое заданное значение поля по блокам — у системы настройка одна на существо. */
    private <T> T first(List<CreatureSpellcastingBlock> blocks,
                        Function<CreatureSpellcastingBlock, T> value) {
        return blocks.stream()
                .map(value)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private void putIfHasText(Map<String, Object> map, String key, String value) {
        if (StringUtils.hasText(value)) {
            map.put(key, value);
        }
    }
}
