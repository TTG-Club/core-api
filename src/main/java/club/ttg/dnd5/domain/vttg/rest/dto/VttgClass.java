package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Класс (character class) в формате компендиума VTTG ({@code type = "class"}).
 *
 * <p>Соответствует целевому формату {@code ClassDefinition} проекта VTTG (см.
 * {@code shared/system/dnd/classTypes.ts} и образцы {@code srd/classes/*.json}):
 * самоописывающаяся запись с постоянным {@code type = "class"}, ключом {@code key},
 * базовой механикой (кость хитов, владения, заклинательство), поуровневой прогрессией
 * ({@link #levelTable}/{@link #tableColumns}), списком умений {@link #features} и
 * вложенными подклассами {@link #subclasses} (в модели TTG Club подклассы — это
 * дочерние классы, при экспорте они сворачиваются внутрь записи родителя).</p>
 *
 * <p>Часть полей эталона отсутствует в модели TTG Club и берётся из канонических карт по
 * ключу класса (см. {@code VttgClassMapper}): заклинательная характеристика и подпись
 * группы подклассов ({@link #subclassLabel}). Счётчики классовых ресурсов ({@code counters})
 * в источнике структурно не хранятся и не выгружаются.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgClass {
    /** Канонический тип сущности для VTTG — всегда "class". */
    private String type;
    /**
     * Идентификатор записи для раскладки дельты: имя файла {@code <id>.json} (см. {@code routeEntity}
     * в VTTG {@code compendiumUpdate.ts}). Для классов совпадает с {@link #key} («по key, без суффикса»).
     */
    private String id;
    /** Слаг листа дерева разделов, в котором показывается запись — всегда "classes". */
    private String section;
    /** Стабильный ключ класса (slug из английского имени, как в {@code spell.classKeys}). */
    private String key;
    private String name;
    private String nameEn;
    private String description;
    /** Ключ источника: "phb"/"dmg"/... (источник в VTTG резолвится из него). */
    private String sourceKey;
    /**
     * Признак принадлежности к SRD (по наличию {@code srdVersion}). Выводится всегда:
     * {@code isSRD === false} уводит запись в premium-пак, отсутствие/‌{@code true} — в SRD-пак.
     */
    private boolean isSRD;

    /** Кость хитов (6/8/10/12). */
    private Integer hitDie;
    /** Владения доспехами (slug'и: "light"/"medium"/"heavy"/"shield"); пустой список при отсутствии. */
    private List<String> armorProficiencies;
    /** Владения оружием ("simple"/"martial"/…); пустой список при отсутствии. */
    private List<String> weaponProficiencies;
    /** Владения инструментами (свободный текст источника одной строкой); пустой список при отсутствии. */
    private List<String> toolProficiencies;
    /** Спасброски (slug'и характеристик: "strength"/…); пустой список при отсутствии. */
    private List<String> savingThrowProficiencies;
    /** Выбор навыков класса. */
    private SkillChoices skillChoices;
    /** Стартовое снаряжение вариантами выбора; опускается, если в источнике пусто. */
    private List<StartingEquipment> startingEquipment;

    /**
     * Конфигурация заклинательства ({@code null} — не заклинатель). Поле выводится всегда
     * (в т.ч. {@code null}), как в эталонных {@code srd/classes/*.json}.
     */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Spellcasting spellcasting;

    /** Уровень выбора подкласса. */
    private Integer subclassLevel;
    /** Подпись группы подклассов («Воинский архетип», «Магическая традиция»). */
    private String subclassLabel;
    /** Подклассы (дочерние классы источника); пустой список при отсутствии. */
    private List<Subclass> subclasses;

    /** Умения класса (все уровни); пустой список при отсутствии. */
    private List<Feature> features;
    /**
     * Таблица прогрессии (уровни 1–20). Каждая запись — {@code Map} с фиксированными полями
     * {@code level}/{@code proficiencyBonus}/{@code featureKeys} и динамическими колонками из
     * {@link #tableColumns} ({@code columnKey → value}).
     */
    private List<Map<String, Object>> levelTable;
    /** Описание дополнительных колонок таблицы; опускается, если их нет. */
    private List<TableColumn> tableColumns;
    /** Владения при взятии класса мультиклассом; опускается, если в источнике не задано. */
    private MulticlassProficiencies multiclassProficiencies;

    /** Явный геттер: без него Jackson сериализует boolean-{@code isSRD} как ключ «SRD» (как в {@code VttgSpell}). */
    @JsonProperty("isSRD")
    public boolean isSRD() {
        return isSRD;
    }

    /** Выбор навыков: сколько ({@code count}) и из какого списка ({@code from}, slug'и навыков). */
    public record SkillChoices(int count, List<String> from) {
    }

    /** Вариант стартового снаряжения: ключ («A»/«B»/…) и человекочитаемое описание. */
    public record StartingEquipment(String key, String description) {
    }

    /** Заклинательство: тип ("full"/"half"/"third"/"pact"), характеристика и стартовый уровень. */
    public record Spellcasting(String type, String ability, Integer startLevel) {
    }

    /** Колонка таблицы прогрессии: ключ значения, подпись и (опционально) под-колонки. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TableColumn(String key, String label, List<TableColumn> children) {
    }

    /**
     * Умение класса/подкласса: стабильный {@code key}, {@code name}, текст {@code description},
     * уровень получения {@code level}, ключ подкласса {@code subclassKey} (для умений подкласса)
     * и варианты выбора {@code choices} (напр. боевые стили). Пустые поля опускаются.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Feature(String key, String name, String description, Integer level,
                          String subclassKey, List<Choice> choices) {
    }

    /** Вариант выбора в рамках умения (боевой стиль, манёвр): {@code key}, {@code name}, {@code description}. */
    public record Choice(String key, String name, String description) {
    }

    /**
     * Подкласс: собственные {@code key}/{@code name}/{@code nameEn}/{@code description},
     * уровень открытия {@code unlockLevel}, умения {@code features} (с проставленным
     * {@code subclassKey}) и, для подклассов с собственной магией/прогрессией,
     * {@code spellcasting}/{@code levelTable}/{@code tableColumns}.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Builder
    @Getter
    public static class Subclass {
        private String key;
        private String name;
        private String nameEn;
        private String description;
        private Integer unlockLevel;
        private String sourceKey;
        private Spellcasting spellcasting;
        private List<Feature> features;
        private List<Map<String, Object>> levelTable;
        private List<TableColumn> tableColumns;
    }

    /** Владения мультикласса: доспехи, оружие, инструменты и число выбираемых навыков. */
    public record MulticlassProficiencies(List<String> armor, List<String> weapons,
                                          List<String> tools, int skillChoices) {
    }
}
