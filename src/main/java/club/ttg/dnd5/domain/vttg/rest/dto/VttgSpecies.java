package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
 * <p>Из модели TTG Club структурно доступны не все награды эталона: в {@link #grants}
 * выгружается только тёмное зрение ({@code darkvision}); сопротивления к урону и владения
 * навыками в источнике хранятся внутри текста умений, поэтому в {@code grants} не попадают.</p>
 *
 * <p>Происхождения (lineages) — это дочерние виды в модели TTG Club; при экспорте они
 * сворачиваются в {@link Feature#choices()} «происхожденческого» умения родителя
 * (см. {@code VttgSpeciesMapper}). {@code grantedSpells} в {@link Feature} нет — модель
 * {@code SpeciesFeature} их не содержит.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgSpecies {
    /** Канонический тип сущности для VTTG — всегда "species". */
    private String type;
    /** Слаг листа дерева разделов, в котором показывается запись — всегда "species". */
    private String section;
    /** Стабильный ключ вида (slug из url). */
    private String key;
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
    public record Grant(String type, Integer range, List<String> damageTypes, Integer count, List<String> from) {
    }

    /**
     * Видовое умение: {@code key} (slug), {@code name}, текст {@code description} и, для
     * «происхожденческих» умений, варианты выбора {@code choices} (происхождения вида).
     * Пустые {@code choices} опускаются.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Feature(String key, String name, String description, List<Choice> choices) {
    }

    /** Вариант происхождения вида (дочерний вид): {@code key} (slug), {@code name}, {@code description}. */
    public record Choice(String key, String name, String description) {
    }
}
