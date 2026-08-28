package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.character_class.model.ArmorProficiency;
import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureOption;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureScaling;
import club.ttg.dnd5.domain.character_class.model.ClassResourceRecovery;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.ClassTableItem;
import club.ttg.dnd5.domain.character_class.model.MulticlassProficiency;
import club.ttg.dnd5.domain.character_class.model.SkillProficiency;
import club.ttg.dnd5.domain.character_class.model.WeaponProficiency;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumnPurpose;
import club.ttg.dnd5.domain.character_class.model.mechanics.ClassMechanics;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.model.mechanics.CounterScaling;
import club.ttg.dnd5.domain.common.model.mechanics.GrantedSpellRef;
import club.ttg.dnd5.domain.common.model.mechanics.ResourceCounter;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.common.rest.dto.Name;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgClass;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgFeatData;
import club.ttg.dnd5.util.SlugifyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Маппер класса TTG Club в формат компендиума VTTG ({@code type = "class"}, эталон
 * {@code ClassDefinition}).
 *
 * <p>Подклассы (дочерние классы) сворачиваются внутрь записи родителя как
 * {@link VttgClass.Subclass}. Поуровневые улучшения умений ({@code scaling}) разворачиваются
 * в отдельные умения на своих уровнях — так их можно адресовать из {@code levelTable.featureKeys}.
 * Таблица прогрессии источника (колонки со значениями по уровням) транспонируется в строки
 * по уровням 1–20; бонус мастерства и состав умений уровня вычисляются здесь.</p>
 *
 * <p>Чего нет в модели TTG Club, берётся из канонических карт по ключу класса:
 * {@link #CASTING_ABILITY} (характеристика заклинателя) и {@link #SUBCLASS_LABEL} (подпись
 * группы подклассов). Стартовый уровень заклинателя и уровень выбора подкласса по правилам
 * PHB 2024 — {@code 1} и {@code 3} соответственно.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgClassMapper {
    private static final String TYPE = "class";
    /** Слаг листа дерева разделов (совпадает с {@code SectionType.CLASS} = "classes"). */
    private static final String SECTION = "classes";
    /** Максимальный уровень персонажа D&D 5e — глубина таблицы прогрессии. */
    private static final int MAX_LEVEL = 20;
    /** Уровень выбора подкласса по умолчанию (PHB 2024). */
    private static final int DEFAULT_SUBCLASS_LEVEL = 3;
    /** Стартовый уровень заклинательства базовых классов (PHB 2024). */
    private static final int SPELLCASTING_START_LEVEL = 1;

    /** Заклинательная характеристика канонических классов (в модели не хранится). */
    private static final Map<String, String> CASTING_ABILITY = Map.of(
            "bard", "charisma",
            "cleric", "wisdom",
            "druid", "wisdom",
            "paladin", "charisma",
            "ranger", "wisdom",
            "sorcerer", "charisma",
            "warlock", "charisma",
            "wizard", "intelligence"
    );

    /** Подпись группы подклассов канонических классов (в модели не хранится). */
    private static final Map<String, String> SUBCLASS_LABEL = Map.ofEntries(
            Map.entry("barbarian", "Первобытный путь"),
            Map.entry("bard", "Бардовская коллегия"),
            Map.entry("cleric", "Божественный домен"),
            Map.entry("druid", "Друидический круг"),
            Map.entry("fighter", "Воинский архетип"),
            Map.entry("monk", "Монашеский орден"),
            Map.entry("paladin", "Священная клятва"),
            Map.entry("ranger", "Архетип следопыта"),
            Map.entry("rogue", "Архетип плута"),
            Map.entry("sorcerer", "Чародейское происхождение"),
            Map.entry("warlock", "Потусторонний покровитель"),
            Map.entry("wizard", "Магическая традиция")
    );

    private final VttgMarkupConverter markupConverter;
    private final VttgEquipmentMapper equipmentMapper;
    private final VttgFeatMechanicsMapper mechanicsMapper;

    public VttgClass toVttg(CharacterClass characterClass) {
        String key = classKey(characterClass);
        List<VttgClass.Feature> features = features(characterClass.getFeatures(), null);
        List<VttgClass.Subclass> subclasses = subclasses(characterClass.getSubclasses());

        return VttgClass.builder()
                .type(TYPE)
                .id(key)
                .section(SECTION)
                .srcSection(SectionType.CLASS.getValue())
                .srcUrl(characterClass.getUrl())
                .key(key)
                .name(characterClass.getName())
                .nameEn(optional(characterClass.getEnglish()))
                .description(description(characterClass.getDescription()))
                .sourceKey(VttgSourceKeys.of(characterClass.getSource()))
                .isSRD(characterClass.getSrdVersion() != null)
                .hitDie(hitDie(characterClass.getHitDice()))
                .armorProficiencies(armor(characterClass.getArmorProficiency()))
                .weaponProficiencies(weapon(characterClass.getWeaponProficiency()))
                .toolProficiencies(tools(characterClass.getToolProficiency()))
                .savingThrowProficiencies(abilities(characterClass.getSavingThrows()))
                .skillChoices(skillChoices(characterClass.getSkillProficiency()))
                .startingEquipment(startingEquipment(characterClass))
                .spellcasting(spellcasting(key, characterClass))
                .subclassLevel(subclassLevel(characterClass, subclasses))
                .subclassLabel(subclassLabel(key, characterClass))
                .subclasses(subclasses)
                .features(features)
                .levelTable(levelTable(characterClass.getTable(), features))
                .tableColumns(tableColumns(characterClass.getTable()))
                .counters(counters(characterClass, subclasses))
                .multiclassProficiencies(multiclass(characterClass.getMulticlassProficiency()))
                .activeEffects(effects(characterClass.getActiveEffects()))
                .featData(featData(characterClass.getMechanics()))
                .build();
    }

    /**
     * Дары записи блоком {@code featData} — тем же, каким уезжают дары черты и предыстории.
     *
     * <p>Требований у класса нет: их проверяет мультиклассирование по ключевым
     * характеристикам, а не список предусловий записи.</p>
     *
     * @param mechanics механика записи или её умения; {@code null} — выдавать нечего
     * @return блок даров либо {@code null}
     */
    private VttgFeatData featData(ClassMechanics mechanics) {
        if (mechanics == null) {
            return null;
        }

        // Заклинания и ресурсы у класса уже выведены своими полями записи
        // ({@code Feature.grantedSpells}, {@code counters}); повтори их блок даров — и
        // потребитель выдал бы то же самое дважды
        ClassMechanics withoutDuplicates = new ClassMechanics();

        withoutDuplicates.setModifiers(mechanics.getModifiers());
        withoutDuplicates.setProficiencies(mechanics.getProficiencies());
        withoutDuplicates.setChoices(mechanics.getChoices());
        withoutDuplicates.setSpellList(mechanics.getSpellList());
        withoutDuplicates.setFeats(mechanics.getFeats());

        return mechanicsMapper.featData(withoutDuplicates, null);
    }

    // ── Счётчики ресурсов ────────────────────────────────────────

    /**
     * Счётчики класса и его подклассов: ресурсы механики записи и её умений, а к ним —
     * колонки таблицы с заданным восстановлением ({@code resourceRecovery}).
     *
     * <p>Колонка-ресурс — прежний способ записи, оставленный ради классов, которые ещё не
     * переписаны на механику: редактор такие колонки больше не заводит. Колонка без
     * восстановления — это обычная колонка прогрессии (ячейки заклинаний, известные
     * заговоры), и счётчиком она не становится.</p>
     */
    private List<VttgClass.Counter> counters(CharacterClass characterClass,
                                             List<VttgClass.Subclass> subclasses) {
        List<VttgClass.Counter> result = new ArrayList<>(
                merged(mechanicsCounters(characterClass, null), counters(characterClass.getTable(), null)));

        Set<String> exported = subclasses.stream()
                .map(VttgClass.Subclass::getKey)
                .collect(Collectors.toSet());

        for (CharacterClass child : subclassSource(characterClass)) {
            String subclassKey = subclassKey(child);
            // Ресурсы берём только у подклассов, попавших в запись: иначе счётчик
            // ссылался бы на подкласс, которого в выгрузке нет
            if (exported.contains(subclassKey)) {
                result.addAll(merged(mechanicsCounters(child, subclassKey),
                        counters(child.getTable(), subclassKey)));
            }
        }
        return result.isEmpty() ? null : result;
    }

    /**
     * Ресурсы механики и колонки-ресурсы одной записи вместе.
     *
     * <p>Колонка и механика — два способа записать один и тот же ресурс, и у класса,
     * который переписывают на механику, какое-то время есть оба. Побеждает механика: её
     * автор и заполняет, а колонка осталась от прежней записи и знает о ресурсе меньше —
     * ни нижней границы максимума, ни порции короткого отдыха у неё нет.</p>
     *
     * @param mechanics счётчики из механики записи и её умений.
     * @param columns счётчики из колонок таблицы прогрессии.
     * @return счётчики без повторов по ключу.
     */
    private List<VttgClass.Counter> merged(List<VttgClass.Counter> mechanics,
                                           List<VttgClass.Counter> columns) {
        Set<String> keys = mechanics.stream()
                .map(VttgClass.Counter::key)
                .collect(Collectors.toSet());

        List<VttgClass.Counter> result = new ArrayList<>(mechanics);
        columns.stream()
                .filter(counter -> !keys.contains(counter.key()))
                .forEach(result::add);
        return result;
    }

    /**
     * Счётчики из механики записи и её умений — основной способ записать ресурс класса:
     * максимум формулой или ступенями, нижняя граница максимума и порция короткого
     * отдыха есть только здесь.
     */
    private List<VttgClass.Counter> mechanicsCounters(CharacterClass characterClass, String subclassKey) {
        List<ResourceCounter> counters = new ArrayList<>();
        collectCounters(characterClass.getMechanics(), counters);
        for (ClassFeature feature : Optional.ofNullable(characterClass.getFeatures()).orElse(List.of())) {
            if (feature != null) {
                collectCounters(feature.getMechanics(), counters);
            }
        }

        List<VttgClass.Counter> result = new ArrayList<>();
        for (ResourceCounter counter : counters) {
            Map<String, Integer> progression = counterProgression(counter);
            boolean hasMax = StringUtils.hasText(counter.getMax());
            if (!StringUtils.hasText(counter.getKey()) || (!hasMax && progression == null)) {
                continue;
            }
            result.add(new VttgClass.Counter(counter.getKey(), counterName(counter),
                    optional(counter.getShortName()), counterStartLevel(counter),
                    VttgDictionaries.recovery(counter.resolveRecovery()),
                    progression, hasMax ? counter.getMax() : null, counter.resolveMin(), subclassKey));
        }
        return result;
    }

    /**
     * Ступени максимума счётчика прогрессией по уровням: у потребителя это тот же вид
     * записи, что и у колонки таблицы, и ему всё равно, откуда ряд пришёл.
     *
     * @param counter ресурс из механики.
     * @return прогрессия по уровням; {@code null} — ступеней нет.
     */
    private Map<String, Integer> counterProgression(ResourceCounter counter) {
        if (CollectionUtils.isEmpty(counter.getScaling())) {
            return null;
        }
        Map<String, Integer> progression = new LinkedHashMap<>();
        counter.getScaling().stream()
                .filter(step -> step != null && step.getLevel() != null && step.getMax() != null)
                .sorted(Comparator.comparingInt(CounterScaling::getLevel))
                .forEach(step -> progression.put(String.valueOf(step.getLevel()), step.getMax()));
        return progression.isEmpty() ? null : progression;
    }

    /**
     * Уровень, с которого счётчик появляется: первая ступень, а без ступеней — первый.
     *
     * <p>Ресурс со ступенями до первой из них не существует вовсе: кости превосходства
     * появляются на третьем уровне вместе с подклассом, и счётчик «0 из 0» на первых двух
     * уровнях листа только мешал бы.</p>
     *
     * @param counter ресурс из механики.
     * @return уровень появления счётчика.
     */
    private int counterStartLevel(ResourceCounter counter) {
        if (CollectionUtils.isEmpty(counter.getScaling())) {
            return 1;
        }
        return counter.getScaling().stream()
                .filter(step -> step != null && step.getLevel() != null && step.getMax() != null)
                .mapToInt(CounterScaling::getLevel)
                .min()
                .orElse(1);
    }

    private void collectCounters(ClassMechanics mechanics, List<ResourceCounter> target) {
        if (mechanics != null && !CollectionUtils.isEmpty(mechanics.getCounters())) {
            target.addAll(mechanics.getCounters());
        }
    }

    /** Подпись счётчика: своя, иначе ключ — иначе плитка на листе осталась бы безымянной. */
    private String counterName(ResourceCounter counter) {
        return StringUtils.hasText(counter.getName()) ? counter.getName() : counter.getKey();
    }

    /** Дочерние классы источника без скрытых — тот же отбор, что и у {@link #subclasses}. */
    private List<CharacterClass> subclassSource(CharacterClass characterClass) {
        Collection<CharacterClass> children = characterClass.getSubclasses();
        if (children == null) {
            return List.of();
        }
        return children.stream()
                .filter(Objects::nonNull)
                .filter(child -> !child.isHiddenEntity())
                .toList();
    }

    /** Счётчики одной таблицы прогрессии. */
    private List<VttgClass.Counter> counters(List<ClassTableColumn> columns, String subclassKey) {
        if (columns == null) {
            return List.of();
        }
        List<VttgClass.Counter> result = new ArrayList<>();
        for (ClassTableColumn column : columns) {
            VttgClass.Counter counter = counter(column, subclassKey);
            if (counter != null) {
                result.add(counter);
            }
        }
        return result;
    }

    /**
     * Счётчик из колонки таблицы. Прогрессия собирается только из числовых значений:
     * колонка ресурса может нести прочерк или «безлимит» — такие уровни в максимум не
     * переводятся, и на них счётчик просто не меняется.
     */
    private VttgClass.Counter counter(ClassTableColumn column, String subclassKey) {
        if (column == null || !StringUtils.hasText(column.getName())
                || column.getResourceRecovery() == null
                || column.getResourceRecovery() == ClassResourceRecovery.NONE
                || column.getScaling() == null) {
            return null;
        }

        Map<String, Integer> progression = new LinkedHashMap<>();
        int startLevel = Integer.MAX_VALUE;

        for (ClassTableItem item : column.getScaling()) {
            if (item == null || !StringUtils.hasText(item.getValue())) {
                continue;
            }
            Integer value = numeric(item.getValue());
            if (value == null) {
                continue;
            }
            progression.put(String.valueOf(item.getLevel()), value);
            startLevel = Math.min(startLevel, item.getLevel());
        }

        if (progression.isEmpty()) {
            return null;
        }

        return new VttgClass.Counter(columnKey(column), column.getName(),
                optional(column.getShortName()), startLevel, recovery(column.getResourceRecovery()),
                progression, null, null, subclassKey);
    }

    /** Способ восстановления в словаре потребителя. */
    private String recovery(ClassResourceRecovery recovery) {
        return recovery == ClassResourceRecovery.SHORT_REST ? "short" : "long";
    }

    /** Целое из значения ячейки; нечисловое («—», «∞») даёт {@code null}. */
    private Integer numeric(String value) {
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    // ── Подклассы ────────────────────────────────────────────────

    /** Видимые подклассы (дочерние классы) в порядке имени. */
    private List<VttgClass.Subclass> subclasses(Collection<CharacterClass> subclasses) {
        if (subclasses == null) {
            return List.of();
        }
        return subclasses.stream()
                .filter(Objects::nonNull)
                .filter(subclass -> !subclass.isHiddenEntity())
                .sorted(Comparator.comparing(CharacterClass::getName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(this::subclass)
                .toList();
    }

    private VttgClass.Subclass subclass(CharacterClass subclass) {
        String key = subclassKey(subclass);
        List<VttgClass.Feature> features = features(subclass.getFeatures(), key);
        return VttgClass.Subclass.builder()
                .key(key)
                .name(subclass.getName())
                .nameEn(optional(subclass.getEnglish()))
                .description(description(subclass.getDescription()))
                .unlockLevel(unlockLevel(features))
                .sourceKey(VttgSourceKeys.of(subclass.getSource()))
                .spellcasting(spellcasting(null, subclass))
                .features(features)
                .levelTable(hasTable(subclass.getTable()) ? levelTable(subclass.getTable(), features) : null)
                .tableColumns(tableColumns(subclass.getTable()))
                .activeEffects(effects(subclass.getActiveEffects()))
                .featData(featData(subclass.getMechanics()))
                .build();
    }

    /** Уровень открытия подкласса — минимальный уровень его умений (иначе {@code 3}). */
    private Integer unlockLevel(List<VttgClass.Feature> features) {
        return features.stream()
                .map(VttgClass.Feature::level)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(DEFAULT_SUBCLASS_LEVEL);
    }

    // ── Умения ───────────────────────────────────────────────────

    /**
     * Умения класса/подкласса. Каждое умение источника даёт основную запись на своём уровне,
     * а каждый элемент {@code scaling} — отдельную запись на своём уровне (плоская модель эталона).
     * Порядок: по уровню, затем в порядке источника.
     */
    private List<VttgClass.Feature> features(List<ClassFeature> features, String subclassKey) {
        if (features == null) {
            return List.of();
        }
        List<VttgClass.Feature> result = new ArrayList<>();
        for (ClassFeature feature : features) {
            if (feature == null) {
                continue;
            }
            String key = featureKey(feature);
            result.add(new VttgClass.Feature(key, feature.getName(),
                    description(feature.getDescription()), feature.getLevel(),
                    subclassKey, choices(feature.getOptions()),
                    flag(feature.isAbilityImprovement()), flag(feature.isFightingStyleChoice()),
                    featureSkillChoice(feature.getSkillChoice()), flag(feature.isInformationalOnly()),
                    grantedSpells(feature.getMechanics()), effects(feature.getActiveEffects()),
                    featData(feature.getMechanics())));
            appendScaling(result, feature, key, subclassKey);
        }
        result.sort(Comparator.comparing(feature -> feature.level() == null ? 0 : feature.level()));
        return result;
    }

    /** Разворачивает {@code scaling} умения в отдельные записи (ключ — {@code <feature>-<level>}). */
    private void appendScaling(List<VttgClass.Feature> target, ClassFeature feature,
                               String baseKey, String subclassKey) {
        if (feature.getScaling() == null) {
            return;
        }
        for (ClassFeatureScaling scaling : feature.getScaling()) {
            if (scaling == null) {
                continue;
            }
            String name = StringUtils.hasText(scaling.getName()) ? scaling.getName() : feature.getName();
            target.add(new VttgClass.Feature(baseKey + "-" + scaling.getLevel(), name,
                    description(scaling.getDescription()), scaling.getLevel(), subclassKey, null));
        }
    }

    private List<VttgClass.Choice> choices(List<ClassFeatureOption> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        List<VttgClass.Choice> result = new ArrayList<>();
        for (ClassFeatureOption option : options) {
            if (option == null) {
                continue;
            }
            result.add(new VttgClass.Choice(optionKey(option), optionName(option.getName()),
                    description(option.getDescription())));
        }
        return result.isEmpty() ? null : result;
    }

    // ── Таблица прогрессии ───────────────────────────────────────

    /**
     * Таблица прогрессии по уровням 1–20: {@code level}, бонус мастерства, ключи умений уровня
     * ({@code featureKeys}) и динамические колонки источника (значение соответствующего уровня).
     */
    private List<Map<String, Object>> levelTable(List<ClassTableColumn> columns, List<VttgClass.Feature> features) {
        Map<Integer, List<String>> keysByLevel = featureKeysByLevel(features);
        Map<String, Map<Integer, String>> columnValues = columnValues(columns);
        Map<Integer, Integer> newCantrips = newByLevel(columns, ClassTableColumnPurpose.CANTRIPS_KNOWN);
        Map<Integer, Integer> newSpells = newByLevel(columns, ClassTableColumnPurpose.SPELLS_KNOWN,
                ClassTableColumnPurpose.PREPARED_SPELLS);

        List<Map<String, Object>> table = new ArrayList<>();
        for (int level = 1; level <= MAX_LEVEL; level++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("level", level);
            row.put("proficiencyBonus", proficiencyBonus(level));
            row.put("featureKeys", keysByLevel.getOrDefault(level, List.of()));
            putIfPresent(row, "newCantrips", newCantrips.get(level));
            putIfPresent(row, "newSpells", newSpells.get(level));
            for (Map.Entry<String, Map<Integer, String>> column : columnValues.entrySet()) {
                String value = column.getValue().get(level);
                if (value != null) {
                    row.put(column.getKey(), value);
                }
            }
            table.add(row);
        }
        return table;
    }

    private void putIfPresent(Map<String, Object> row, String key, Integer value) {
        if (value != null && value > 0) {
            row.put(key, value);
        }
    }

    /**
     * Сколько НОВЫХ заговоров или заклинаний игрок выбирает на каждом уровне.
     *
     * <p>В таблице класса такая колонка хранит итог («известно заговоров: 3, 3, 3, 4…»), а
     * мастер повышения уровня спрашивает прирост. Прирост и считается — разностью с
     * предыдущим заполненным уровнем; убыль (её не бывает у канонических классов, но
     * бывает у самописных) в вопрос не превращается.</p>
     *
     * <p>Колонки без назначения ({@link ClassTableColumnPurpose#NONE}) в расчёт не идут:
     * подпись угадывать нельзя — на переведённом классе она любая.</p>
     */
    private Map<Integer, Integer> newByLevel(List<ClassTableColumn> columns,
                                             ClassTableColumnPurpose... purposes) {
        Map<Integer, Integer> result = new LinkedHashMap<>();
        if (columns == null) {
            return result;
        }
        Set<ClassTableColumnPurpose> wanted = Set.of(purposes);
        for (ClassTableColumn column : columns) {
            if (column == null || column.getScaling() == null
                    || column.getPurpose() == null || !wanted.contains(column.getPurpose())) {
                continue;
            }
            int previous = 0;
            for (ClassTableItem item : sortedByLevel(column.getScaling())) {
                Integer total = item.getValue() == null ? null : numeric(item.getValue());
                if (total == null) {
                    continue;
                }
                int gain = total - previous;
                previous = total;
                if (gain > 0) {
                    result.merge(item.getLevel(), gain, Integer::sum);
                }
            }
        }
        return result;
    }

    /** Значения колонки по возрастанию уровня: в источнике порядок строк не гарантирован. */
    private List<ClassTableItem> sortedByLevel(List<ClassTableItem> scaling) {
        return scaling.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(ClassTableItem::getLevel))
                .toList();
    }

    /** Ключи умений (включая развёрнутый scaling), сгруппированные по уровню получения. */
    private Map<Integer, List<String>> featureKeysByLevel(List<VttgClass.Feature> features) {
        Map<Integer, List<String>> result = new LinkedHashMap<>();
        for (VttgClass.Feature feature : features) {
            if (feature.level() == null) {
                continue;
            }
            result.computeIfAbsent(feature.level(), level -> new ArrayList<>()).add(feature.key());
        }
        return result;
    }

    /** Значения колонок источника по ключу колонки → (уровень → значение). */
    private Map<String, Map<Integer, String>> columnValues(List<ClassTableColumn> columns) {
        Map<String, Map<Integer, String>> result = new LinkedHashMap<>();
        if (columns == null) {
            return result;
        }
        for (ClassTableColumn column : columns) {
            if (column == null || column.getScaling() == null) {
                continue;
            }
            Map<Integer, String> byLevel = new LinkedHashMap<>();
            for (ClassTableItem item : column.getScaling()) {
                if (item != null && StringUtils.hasText(item.getValue())) {
                    byLevel.put(item.getLevel(), item.getValue());
                }
            }
            if (!byLevel.isEmpty()) {
                result.put(columnKey(column), byLevel);
            }
        }
        return result;
    }

    /** Описание дополнительных колонок таблицы (ключ значения + подпись). */
    private List<VttgClass.TableColumn> tableColumns(List<ClassTableColumn> columns) {
        if (columns == null) {
            return null;
        }
        List<VttgClass.TableColumn> result = new ArrayList<>();
        for (ClassTableColumn column : columns) {
            if (column == null || !hasTableValues(column) || !StringUtils.hasText(column.getName())) {
                continue;
            }
            result.add(new VttgClass.TableColumn(columnKey(column), column.getName(), null));
        }
        return result.isEmpty() ? null : result;
    }

    private boolean hasTable(List<ClassTableColumn> columns) {
        return columns != null && columns.stream().anyMatch(this::hasTableValues);
    }

    private boolean hasTableValues(ClassTableColumn column) {
        return column != null && column.getScaling() != null && column.getScaling().stream()
                .anyMatch(item -> item != null && StringUtils.hasText(item.getValue()));
    }

    /** Бонус мастерства по уровню: 2 (1–4), 3 (5–8), 4 (9–12), 5 (13–16), 6 (17–20). */
    private int proficiencyBonus(int level) {
        return 2 + (level - 1) / 4;
    }

    // ── Владения ─────────────────────────────────────────────────

    private List<String> armor(ArmorProficiency proficiency) {
        if (proficiency == null || proficiency.getCategory() == null) {
            return List.of();
        }
        return proficiency.getCategory().stream()
                .filter(Objects::nonNull)
                .map(ArmorCategory::name)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    private List<String> weapon(WeaponProficiency proficiency) {
        if (proficiency == null || proficiency.getCategory() == null) {
            return List.of();
        }
        return proficiency.getCategory().stream()
                .filter(Objects::nonNull)
                .map(this::weaponCategory)
                .distinct()
                .toList();
    }

    /** Категория оружия эталона: рукопашное/дальнобойное сворачивается в simple/martial. */
    private String weaponCategory(WeaponCategory category) {
        return switch (category) {
            case SIMPLE_MELEE, SIMPLE_RANGED -> "simple";
            case MATERIAL_MELEE, MATERIAL_RANGED -> "martial";
            case FIREARM -> "firearm";
            case FUTURISTIC -> "futuristic";
        };
    }

    /** Инструменты источника — свободный текст одной строкой; пустой список при отсутствии. */
    private List<String> tools(String toolProficiency) {
        return StringUtils.hasText(toolProficiency) ? List.of(toolProficiency.trim()) : List.of();
    }

    private List<String> abilities(Set<Ability> abilities) {
        if (abilities == null) {
            return List.of();
        }
        return abilities.stream()
                .filter(Objects::nonNull)
                .map(ability -> ability.name().toLowerCase(Locale.ROOT))
                .toList();
    }

    /**
     * Взведённый флаг умения; снятый опускается ({@code null}).
     *
     * <p>Флаги — это подсказки мастеру повышения уровня, а не свойства записи: «здесь
     * спроси про характеристики». Ложь в выгрузке несла бы ровно ноль сведений и
     * висела бы у каждого умения каждого класса.</p>
     */
    private Boolean flag(boolean value) {
        return value ? Boolean.TRUE : null;
    }

    /**
     * Активные эффекты как есть: {@link ActiveEffect} заполняется в мастерской сразу в
     * вокабуляре VTTG — так же, как у черты и предмета. Пустой список опускается.
     */
    private List<ActiveEffect> effects(List<ActiveEffect> effects) {
        return CollectionUtils.isEmpty(effects) ? null : effects;
    }

    /**
     * Заклинания, которые умение выдаёт без выбора, — списком id (они же url записей
     * справочника), как их ждёт {@code ClassFeature.grantedSpells} эталона.
     *
     * <p>Уровень доступности отдельного заклинания здесь не нужен: у класса гейт задан
     * уровнем самого умения, и вторым уровнем внутри умения он бы только разошёлся с
     * первым.</p>
     */
    private List<String> grantedSpells(ClassMechanics mechanics) {
        if (mechanics == null) {
            return null;
        }
        SpellGrant grant = mechanics.getSpells();
        if (grant == null || CollectionUtils.isEmpty(grant.getSpells())) {
            return null;
        }
        List<String> result = grant.getSpells().stream()
                .filter(Objects::nonNull)
                .map(GrantedSpellRef::getUrl)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        return result.isEmpty() ? null : result;
    }

    /**
     * Выбор навыков у САМОГО умения. В отличие от {@link #skillChoices(SkillProficiency)}
     * пустое значение здесь опускается: у класса блок выбора есть всегда (пусть и с
     * нулём), а у умения его отсутствие и означает «умение навыков не даёт».
     */
    private VttgClass.SkillChoices featureSkillChoice(SkillProficiency proficiency) {
        if (proficiency == null || proficiency.getCount() <= 0) {
            return null;
        }
        return skillChoices(proficiency);
    }

    private VttgClass.SkillChoices skillChoices(SkillProficiency proficiency) {
        if (proficiency == null) {
            return new VttgClass.SkillChoices(0, List.of());
        }
        List<String> from = proficiency.getSkills() == null ? List.of()
                : proficiency.getSkills().stream()
                        .filter(Objects::nonNull)
                        .map(this::skillKey)
                        .toList();
        return new VttgClass.SkillChoices(proficiency.getCount(), from);
    }

    private VttgClass.MulticlassProficiencies multiclass(MulticlassProficiency multiclass) {
        if (multiclass == null) {
            return null;
        }
        return new VttgClass.MulticlassProficiencies(
                armor(multiclass.getArmor()),
                weapon(multiclass.getWeapon()),
                tools(multiclass.getToolProficiency()),
                multiclass.getSkills());
    }

    // ── Прочее ───────────────────────────────────────────────────

    /**
     * Стартовое снаряжение. Основной источник — структурированное {@code startingEquipment}
     * (варианты «А», «Б», … с предметами и монетами): именно его показывает сайт, и только из
     * него получаются ссылки на карточки предметов. Свободный текст {@code equipment} —
     * легаси-запас для записей, которые на структуру ещё не перевели; он выгружается одним
     * вариантом. Нет ни того, ни другого — {@code null} (поле опускается).
     */
    private List<VttgClass.StartingEquipment> startingEquipment(CharacterClass characterClass) {
        List<VttgEquipmentMapper.RenderedOption> rendered =
                equipmentMapper.render(characterClass.getStartingEquipment());
        if (!rendered.isEmpty()) {
            List<VttgClass.StartingEquipment> options = new ArrayList<>(rendered.size());
            for (int index = 0; index < rendered.size(); index++) {
                VttgEquipmentMapper.RenderedOption option = rendered.get(index);
                options.add(new VttgClass.StartingEquipment(equipmentMapper.label(index),
                        option.description(), option.items(), option.coins(), option.coin()));
            }
            return options;
        }

        String text = description(characterClass.getEquipment());
        return StringUtils.hasText(text)
                ? List.of(new VttgClass.StartingEquipment(equipmentMapper.label(0), text))
                : null;
    }

    /** Заклинательство: {@code null}, если класс не заклинатель или неизвестна характеристика. */
    private VttgClass.Spellcasting spellcasting(String classKey, CharacterClass characterClass) {
        String type = casterTypeKey(characterClass.getCasterType());
        if (type == null) {
            return null;
        }
        String ability = castingAbility(classKey, characterClass);
        if (ability == null) {
            return null;
        }
        Integer startLevel = characterClass.getSpellcastingStartLevel();
        return new VttgClass.Spellcasting(type, ability,
                startLevel == null ? SPELLCASTING_START_LEVEL : startLevel);
    }

    /**
     * Заклинательная характеристика: своя у записи, иначе каноническая по ключу класса.
     *
     * <p>Карта осталась запасным вариантом ради записей, сохранённых до появления поля:
     * у переведённого или самописного класса её в карте нет, и до появления поля вся
     * заклинательная конфигурация у него молча пропадала.</p>
     */
    private String castingAbility(String classKey, CharacterClass characterClass) {
        Ability ability = characterClass.getSpellcastingAbility();
        if (ability != null) {
            return ability.name().toLowerCase(Locale.ROOT);
        }
        return classKey == null ? null : CASTING_ABILITY.get(classKey);
    }

    private String casterTypeKey(CasterType casterType) {
        if (casterType == null) {
            return null;
        }
        return switch (casterType) {
            case FULL -> "full";
            case HALF -> "half";
            case THIRD -> "third";
            case PACT -> "pact";
            case MULTICLASS, NONE -> null;
        };
    }

    private Integer subclassLevel(CharacterClass characterClass, List<VttgClass.Subclass> subclasses) {
        if (characterClass.getSubclassLevel() != null) {
            return characterClass.getSubclassLevel();
        }
        return subclasses.stream()
                .map(VttgClass.Subclass::getUnlockLevel)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(DEFAULT_SUBCLASS_LEVEL);
    }

    private String subclassLabel(String classKey, CharacterClass characterClass) {
        if (StringUtils.hasText(characterClass.getSubclassLabel())) {
            return characterClass.getSubclassLabel();
        }
        return classKey == null ? null : SUBCLASS_LABEL.getOrDefault(classKey, "Подкласс");
    }

    private Integer hitDie(Dice hitDice) {
        return hitDice == null ? null : hitDice.getMaxValue();
    }

    private String description(String markup) {
        String text = markupConverter.toText(markup);
        return StringUtils.hasText(text) ? text : null;
    }


    /** Ключ класса: как в {@code spell.classKeys} — транслит/slug английского имени, иначе из url. */
    private String classKey(CharacterClass characterClass) {
        String english = characterClass.getEnglish();
        if (StringUtils.hasText(english)) {
            return SlugifyUtil.getSlug(english);
        }
        return slug(characterClass.getUrl());
    }

    private String subclassKey(CharacterClass subclass) {
        String english = subclass.getEnglish();
        if (StringUtils.hasText(english)) {
            return SlugifyUtil.getSlug(english);
        }
        return slug(subclass.getUrl());
    }

    private String featureKey(ClassFeature feature) {
        if (StringUtils.hasText(feature.getKey())) {
            return feature.getKey();
        }
        return StringUtils.hasText(feature.getName()) ? SlugifyUtil.getSlug(feature.getName()) : "feature";
    }

    private String optionKey(ClassFeatureOption option) {
        if (StringUtils.hasText(option.getKey())) {
            return option.getKey();
        }
        String name = optionName(option.getName());
        return StringUtils.hasText(name) ? SlugifyUtil.getSlug(name) : "option";
    }

    private String optionName(Name name) {
        if (name == null) {
            return null;
        }
        return StringUtils.hasText(name.getName()) ? name.getName() : name.getEnglish();
    }

    /**
     * Ключ колонки таблицы: заданный в источнике, иначе выведенный из подписи
     * (транслит-slug, чтобы кириллица не схлопывалась в пустоту).
     *
     * <p>Явный ключ нужен ресурсам: по нему лист хранит потраченный остаток, и перевод
     * подписи не должен обнулять счётчики на уже сохранённых листах.</p>
     */
    private String columnKey(ClassTableColumn column) {
        if (StringUtils.hasText(column.getKey())) {
            return column.getKey();
        }
        String slug = SlugifyUtil.getSlug(column.getName() == null ? "" : column.getName());
        return StringUtils.hasText(slug) ? slug : "col";
    }

    /** SNAKE_CASE имя навыка ({@code ANIMAL_HANDLING}) → camelCase slug эталона ({@code animalHandling}). */
    private String skillKey(Skill skill) {
        String[] parts = skill.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                builder.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
            }
        }
        return builder.toString();
    }

    private String slug(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String optional(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
