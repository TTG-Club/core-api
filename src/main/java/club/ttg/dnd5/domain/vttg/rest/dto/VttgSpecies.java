package club.ttg.dnd5.domain.vttg.rest.dto;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Вид (species) в формате компендиума VTTG.
 *
 * <p>Соответствует целевому формату SRD-бэкапа VTTG (см. {@code species/*.json}):
 * самоописывающаяся запись с постоянным {@code type = "species"}, ключом {@code key},
 * характеристиками существа ({@code creatureType}/{@code size}/{@code speed}), дарами
 * записи ({@code featData}) и {@code features} (видовые умения со своими дарами).</p>
 *
 * <p>Дары уезжают тем же блоком {@link VttgFeatData}, что у черты, предыстории и класса:
 * {@code featData} записи — механика самой записи, {@code featData} умения — механика
 * этого умения. Так потребитель видит, какое именно умение дало владение или чувство.
 * Умение, действие которого описано только текстом, даров не даёт.</p>
 *
 * <p>Происхождения (lineages) — это дочерние виды в модели TTG Club; каждый экспортируется
 * самостоятельной записью со ссылкой {@link #parentKey} на родителя. Комбинирование
 * «вид + происхождение» — забота потребителя (см. {@code VttgSpeciesMapper}).</p>
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
    /**
     * Ключ родительского вида для происхождения (slug из url родителя); {@code null} —
     * запись верхнеуровневая. По нему потребитель собирает список происхождений вида.
     */
    private String parentKey;
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
    /**
     * Рост в футах по размерам: ключ — тот же slug, что в {@link #size}, значение — границы
     * «от»/«до». Величина справочная: потребитель показывает её рядом с размером, чтобы
     * игроку было по чему выбирать, когда размеров у вида несколько.
     *
     * <p>Опускается целиком, когда рост не задан ни одному размеру; размер без границ в
     * карту не попадает.</p>
     */
    private Map<String, Height> heights;
    private Speed speed;
    /**
     * Обычное зрение в футах — дальность зрения токена в дневном режиме. Опускается,
     * когда не задано: у потребителя остаётся его значение по умолчанию.
     */
    private Integer vision;
    /**
     * Дары самой записи — механика вида или происхождения целиком, блоком
     * {@link VttgFeatData} (как у черты). Опускается, когда давать нечего.
     */
    private VttgFeatData featData;
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
     * Границы роста одного размера в футах. Задана бывает и одна из двух — у «Среднего,
     * от 5 фт.» верхней границы попросту нет, поэтому пустая граница опускается.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Height(Integer from, Integer to) {
    }

    /**
     * Видовое умение: {@code key} (slug), {@code name}, текст {@code description} и дары
     * умения блоком {@code featData} — той же формы, что у черты.
     *
     * <p>{@code level} — уровень персонажа, с которого умение действует (по умолчанию
     * первый), {@code grantedSpells} — заклинания, которые оно выдаёт: они приходят из
     * {@code mechanics.spells} самого умения. Врождённые заклинания вида в источнике к
     * умению не привязаны — они лежат отдельной таблицей связей, поэтому при экспорте
     * собираются в отдельные умения, по одному на требуемый уровень
     * (см. {@code VttgSpeciesMapper}).</p>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Feature(String key, String name, String description,
                          Integer level, List<GrantedSpell> grantedSpells,
                          List<ActiveEffect> activeEffects, VttgFeatData featData) {
    }

    /**
     * Заклинание, выдаваемое умением вида.
     *
     * @param name    название заклинания — показывается, даже если записи нет в паках
     * @param spellId {@code id} записи заклинания в выгрузке (он же {@code url} на сайте)
     */
    public record GrantedSpell(String name, String spellId) {
    }
}
