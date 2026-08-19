package club.ttg.dnd5.domain.vttg.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Разобранное требование черты в формате компендиума VTTG.
 *
 * <p>Заполненные поля соединяются по «И», множественность внутри поля — по «ИЛИ».
 * Потребитель проверяет требования МЯГКО: показывает, чему персонаж не соответствует,
 * но взять черту не мешает — за столом мастер разрешает исключения, а жёсткий запрет
 * сломал бы уже собранных персонажей.</p>
 *
 * <p>Что проверить нечем ({@code campaign}, свободный текст), отдаётся ради показа —
 * иначе требование пропадает совсем: в описании черты его нет, оно живёт отдельной
 * строкой записи ({@code Feat.prerequisite}) и доезжает сюда полем {@link #text}.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgFeatPrerequisite {
    /** Минимальный суммарный уровень персонажа */
    private Integer minLevel;
    /** Требования к значениям характеристик («Сила ИЛИ Ловкость 13+») */
    private List<AbilityRequirement> abilityRequirements;
    /** Черты, которые нужно иметь */
    private List<VttgEntityRef> feats;
    /** Классы, любого из которых достаточно */
    private List<VttgEntityRef> classes;
    /** Виды, любого из которых достаточно */
    private List<VttgEntityRef> species;
    /** Предыстории, любой из которых достаточно */
    private List<VttgEntityRef> backgrounds;
    /**
     * Классовые умения, любого из которых достаточно:
     * {@code spellcasting}, {@code pactMagic}, {@code fightingStyle}, {@code weaponMastery}.
     */
    private List<String> classFeatures;
    /** Владение доспехами: {@code light}/{@code medium}/{@code heavy}/{@code shield} */
    private List<String> armorProficiency;
    /** Требуется любая черта метки дракона */
    private Boolean anyDragonmark;
    /** Сеттинг кампании — показывается, не проверяется */
    private String campaign;
    /**
     * Требование текстом: то, что не разобралось в поля («Эльф или полуэльф», «превращение
     * в лича»). Показывается, но не проверяется.
     *
     * <p>Поле называется {@code text}, как у потребителя, а не {@code custom}, как в модели
     * источника: имя поля здесь — часть контракта листа, и под другим именем строка до него
     * не доходила.</p>
     */
    private String text;

    /**
     * Требование к значению характеристики.
     *
     * @param anyOf    характеристики, любой из которых достаточно
     * @param minValue минимальное значение
     */
    public record AbilityRequirement(List<String> anyOf, Integer minValue) {
    }
}
