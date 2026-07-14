package club.ttg.dnd5.domain.vttg.service;

import club.ttg.dnd5.domain.character_class.model.ArmorProficiency;
import club.ttg.dnd5.domain.character_class.model.CasterType;
import club.ttg.dnd5.domain.character_class.model.CharacterClass;
import club.ttg.dnd5.domain.character_class.model.ClassFeature;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureOption;
import club.ttg.dnd5.domain.character_class.model.ClassFeatureScaling;
import club.ttg.dnd5.domain.character_class.model.ClassTableColumn;
import club.ttg.dnd5.domain.character_class.model.ClassTableItem;
import club.ttg.dnd5.domain.character_class.model.MulticlassProficiency;
import club.ttg.dnd5.domain.character_class.model.SkillProficiency;
import club.ttg.dnd5.domain.character_class.model.WeaponProficiency;
import club.ttg.dnd5.domain.common.dictionary.Ability;
import club.ttg.dnd5.domain.common.dictionary.ArmorCategory;
import club.ttg.dnd5.domain.common.dictionary.Dice;
import club.ttg.dnd5.domain.common.dictionary.Skill;
import club.ttg.dnd5.domain.common.dictionary.WeaponCategory;
import club.ttg.dnd5.domain.common.rest.dto.Name;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgClass;
import club.ttg.dnd5.util.SlugifyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    /** Запасной ключ источника, если у класса его нет. */
    private static final String SOURCE = "srd";
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

    public VttgClass toVttg(CharacterClass characterClass) {
        String key = classKey(characterClass);
        List<VttgClass.Feature> features = features(characterClass.getFeatures(), null);
        List<VttgClass.Subclass> subclasses = subclasses(characterClass.getSubclasses());

        return VttgClass.builder()
                .type(TYPE)
                .id(key)
                .section(SECTION)
                .key(key)
                .name(characterClass.getName())
                .nameEn(optional(characterClass.getEnglish()))
                .description(description(characterClass.getDescription()))
                .sourceKey(sourceKey(characterClass.getSource()))
                // Как и остальные мапперы выгрузки, помечаем контент как SRD → запись едет в SRD-пак
                // (иначе routeEntity уводит её в premium-пак «TTG Club», отдельно от заклинаний/существ).
                .isSRD(true)
                .hitDie(hitDie(characterClass.getHitDice()))
                .armorProficiencies(armor(characterClass.getArmorProficiency()))
                .weaponProficiencies(weapon(characterClass.getWeaponProficiency()))
                .toolProficiencies(tools(characterClass.getToolProficiency()))
                .savingThrowProficiencies(abilities(characterClass.getSavingThrows()))
                .skillChoices(skillChoices(characterClass.getSkillProficiency()))
                .startingEquipment(startingEquipment(characterClass.getEquipment()))
                .spellcasting(spellcasting(key, characterClass.getCasterType()))
                .subclassLevel(subclassLevel(subclasses))
                .subclassLabel(subclassLabel(key))
                .subclasses(subclasses)
                .features(features)
                .levelTable(levelTable(characterClass.getTable(), features))
                .tableColumns(tableColumns(characterClass.getTable()))
                .multiclassProficiencies(multiclass(characterClass.getMulticlassProficiency()))
                .build();
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
                .sourceKey(sourceKey(subclass.getSource()))
                .spellcasting(spellcasting(null, subclass.getCasterType()))
                .features(features)
                .levelTable(hasTable(subclass.getTable()) ? levelTable(subclass.getTable(), features) : null)
                .tableColumns(tableColumns(subclass.getTable()))
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
                    subclassKey, choices(feature.getOptions())));
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

        List<Map<String, Object>> table = new ArrayList<>();
        for (int level = 1; level <= MAX_LEVEL; level++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("level", level);
            row.put("proficiencyBonus", proficiencyBonus(level));
            row.put("featureKeys", keysByLevel.getOrDefault(level, List.of()));
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
                result.put(columnKey(column.getName()), byLevel);
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
            result.add(new VttgClass.TableColumn(columnKey(column.getName()), column.getName(), null));
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
     * Стартовое снаряжение. В модели TTG Club это единый markdown-текст (без структуры A/Б/В),
     * поэтому выгружается одним вариантом; при отсутствии — {@code null} (поле опускается).
     */
    private List<VttgClass.StartingEquipment> startingEquipment(String equipment) {
        String text = description(equipment);
        return StringUtils.hasText(text) ? List.of(new VttgClass.StartingEquipment("A", text)) : null;
    }

    /** Заклинательство: {@code null}, если класс не заклинатель или неизвестна характеристика. */
    private VttgClass.Spellcasting spellcasting(String classKey, CasterType casterType) {
        String type = casterTypeKey(casterType);
        if (type == null) {
            return null;
        }
        String ability = classKey == null ? null : CASTING_ABILITY.get(classKey);
        if (ability == null) {
            return null;
        }
        return new VttgClass.Spellcasting(type, ability, SPELLCASTING_START_LEVEL);
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

    private Integer subclassLevel(List<VttgClass.Subclass> subclasses) {
        return subclasses.stream()
                .map(VttgClass.Subclass::getUnlockLevel)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(DEFAULT_SUBCLASS_LEVEL);
    }

    private String subclassLabel(String classKey) {
        return classKey == null ? null : SUBCLASS_LABEL.getOrDefault(classKey, "Подкласс");
    }

    private Integer hitDie(Dice hitDice) {
        return hitDice == null ? null : hitDice.getMaxValue();
    }

    private String description(String markup) {
        String text = markupConverter.toText(markup);
        return StringUtils.hasText(text) ? text : null;
    }

    private String sourceKey(Source source) {
        if (source == null) {
            return SOURCE;
        }
        if ("PHB24".equalsIgnoreCase(source.getAcronym())) {
            return "phb";
        }
        return StringUtils.hasText(source.getAcronym())
                ? source.getAcronym().toLowerCase(Locale.ROOT)
                : SOURCE;
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

    /** Ключ колонки таблицы из её подписи (транслит-slug, чтобы кириллица не схлопывалась в пустоту). */
    private String columnKey(String name) {
        String slug = SlugifyUtil.getSlug(name == null ? "" : name);
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
