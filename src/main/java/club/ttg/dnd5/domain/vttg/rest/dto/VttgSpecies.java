package club.ttg.dnd5.domain.vttg.rest.dto;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Вид (species) в формате компендиума VTTG.
 *
 * <p>Соответствует целевому формату SRD-бэкапа VTTG (см. {@code species/*.json}):
 * самоописывающаяся запись с постоянным {@code type = "species"}, ключом {@code key},
 * характеристиками существа ({@code creatureType}/{@code size}/{@code speed}), списком
 * {@code grants} (структурные награды вида) и {@code features} (видовые умения).</p>
 *
 * <p>{@link #grants} собираются из двух мест источника: тёмное зрение ({@code darkvision}) —
 * свойство вида, сопротивления ({@code resistance}) и владения навыками
 * ({@code skillProficiency}) — механика его умений. Умение, действие которого описано
 * только текстом, наград не даёт.</p>
 *
 * <p>Происхождения (lineages) — это дочерние виды в модели TTG Club; при экспорте они
 * сворачиваются в {@link Feature#choices()} «происхожденческого» умения родителя
 * (см. {@code VttgSpeciesMapper}).</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgSpecies {
    /** Канонический тип сущности для VTTG — всегда "species". */
    private String type;
    /**
     * Идентификатор записи для раскладки дельты: имя файла {@code <id>.json} (см. {@code routeEntity}
     * в VTTG {@code compendiumUpdate.ts}). Обязателен, иначе запись отбрасывается. Совпадает с {@link #key}.
     */
    private String id;
    /** Слаг листа дерева разделов, в котором показывается запись — всегда "species". */
    private String section;
    /**
     * Раздел сайта в адресе страницы-источника ({@code species} в {@code /species/elf-phb}).
     * По паре {@code srcSection}/{@code srcUrl} VTTG находит запись в компендиуме, когда в
     * описании кликают ссылку.
     */
    private String srcSection;
    /** Слаг страницы-источника на сайте; с {@code srcSection} составляет адрес ссылки. */
    private String srcUrl;
    /** Стабильный ключ вида (slug из url). */
    private String key;
    /** Признак SRD (для раскладки по пакам); выводится всегда. */
    private boolean isSRD;
    private String name;
    private String nameEn;
    private String description;
    /** Ключ источника: "phb"/"dmg"/... (источник в VTTG резолвится из него). */
    private String sourceKey;
    /** Тип существа (slug): "humanoid"/"dragon"/"fiend"/... */
    private String creatureType;
    /** Размеры в порядке источника (slug'и: "small"/"medium"/...). */
    private List<String> size;
    private Speed speed;
    /** Структурные награды вида; пустой список, если их нет (эталон выгружает {@code []}). */
    private List<Grant> grants;
    /** Видовые умения; пустой список, если их нет. */
    private List<Feature> features;

    /**
     * Активные эффекты вида в вокабуляре VTTG. Отдаются без преобразования — так же, как
     * у черты ({@code VttgFeat.activeEffects}): мастерская заполняет их сразу в словаре
     * VTTG. Опускаются, когда эффектов нет.
     */
    private List<ActiveEffect> activeEffects;

    /** Явный геттер: без него Jackson сериализует boolean-{@code isSRD} как ключ «SRD». */
    @JsonProperty("isSRD")
    public boolean isSRD() {
        return isSRD;
    }

    /** Скорости перемещения в футах; отсутствующие виды движения опускаются. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Speed(Integer walk, Integer fly, Integer climb, Integer swim) {
    }

    /**
     * Награда вида в формате эталона. Поля заполняются в зависимости от {@code type}:
     * {@code darkvision} → {@code range}; {@code resistance} → {@code damageTypes};
     * {@code skillProficiency} → {@code count}/{@code from}. Пустые поля опускаются.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Grant(String type, Integer range, List<String> damageTypes, Integer count, List<String> from,
                        List<DamageDefense> entries, List<String> conditions, List<String> abilities,
                        List<String> items, Choices choices) {

        /** Награда прежней формы: тёмное зрение, сопротивления, выбор навыков. */
        public Grant(String type, Integer range, List<String> damageTypes, Integer count, List<String> from) {
            this(type, range, damageTypes, count, from, null, null, null, null, null);
        }

        /** Безвыборная выдача списком: владения оружием, доспехами, инструментами, языки. */
        public static Grant items(String type, List<String> items) {
            return new Grant(type, null, null, null, null, null, null, null, items, null);
        }

        /** Выдача с выбором: {@code items} — то, что дано сразу, {@code choices} — что выбирают. */
        public static Grant choices(String type, List<String> items, Choices choices) {
            return new Grant(type, null, null, null, null, null, null, null, items, choices);
        }
    }

    /**
     * Защита вида по одному типу урона.
     *
     * @param damageType slug типа урона ({@code fire}, {@code poison})
     * @param kind       вид защиты: {@code resistance}/{@code immunity}/{@code vulnerability}
     */
    public record DamageDefense(String damageType, String kind) {
    }

    /** Выбор внутри награды: сколько ({@code count}) и из чего ({@code from}). */
    public record Choices(int count, List<String> from) {
    }

    /**
     * Видовое умение: {@code key} (slug), {@code name}, текст {@code description} и, для
     * «происхожденческих» умений, варианты выбора {@code choices} (происхождения вида).
     * Пустые {@code choices} опускаются.
     *
     * <p>{@code level} — уровень персонажа, с которого умение действует (по умолчанию
     * первый), {@code grantedSpells} — заклинания, которые оно выдаёт: они приходят из
     * {@code mechanics.spells} самого умения. Врождённые заклинания вида в источнике к
     * умению не привязаны — они лежат отдельной таблицей связей, поэтому при экспорте
     * собираются в отдельные умения, по одному на требуемый уровень
     * (см. {@code VttgSpeciesMapper}).</p>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Feature(String key, String name, String description, List<Choice> choices,
                          Integer level, List<GrantedSpell> grantedSpells,
                          List<ActiveEffect> activeEffects) {

        /** Обычное умение источника: без уровня, заклинаний и эффектов. */
        public Feature(String key, String name, String description, List<Choice> choices) {
            this(key, name, description, choices, null, null, null);
        }
    }

    /**
     * Заклинание, выдаваемое умением вида.
     *
     * @param name    название заклинания — показывается, даже если записи нет в паках
     * @param spellId {@code id} записи заклинания в выгрузке (он же {@code url} на сайте)
     */
    public record GrantedSpell(String name, String spellId) {
    }

    /** Вариант происхождения вида (дочерний вид): {@code key} (slug), {@code name}, {@code description}. */
    public record Choice(String key, String name, String description) {
    }
}
