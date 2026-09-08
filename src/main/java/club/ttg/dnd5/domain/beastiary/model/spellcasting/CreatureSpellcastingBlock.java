package club.ttg.dnd5.domain.beastiary.model.spellcasting;

import club.ttg.dnd5.domain.common.dictionary.Ability;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Блок заклинаний существа: одна заклинательная характеристика, одна Сл и один набор
 * порций.
 *
 * <p>Блоков у существа бывает несколько, и параметры у них разные: у зелёной карги
 * «Магия шабаша» считается от Интеллекта со Сл 11, а собственное «Использование
 * заклинаний» — от Мудрости со Сл 12 и бонусом атаки +4. Поэтому характеристика, Сл и
 * бонус атаки принадлежат блоку, а не существу целиком.</p>
 *
 * <p>Блоки заводятся руками в мастерской и на карточку сайта не выводятся: статблок
 * по-прежнему описывает заклинания текстом записи действия, а блоки — машинные данные
 * для виртуального стола. Разбора описаний здесь нет.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatureSpellcastingBlock {
    /**
     * Уникальный ключ блока. Генерирует и присылает его форма ({@code spellcasting-<uuid>});
     * здесь он только хранится и возвращается как есть — по нему виртуальный стол держится
     * за блок, когда его переименуют или переставят местами. Названия для этого мало: у
     * существа встречается два одинаковых.
     */
    @Schema(description = "Уникальный ключ блока", example = "spellcasting-4d0a4a2e-6c8f-4a1e-9a2b-1f7c2d3e4f50")
    private String id;

    @Schema(description = "Название блока", example = "Использование заклинаний")
    private String name;

    /** Условие блока текстом: числами оно не выразимо, а теряться не должно. */
    @Schema(description = "Условие блока текстом",
            example = "находясь в пределах 30 футов от двух союзных карг")
    private String note;

    @Schema(description = "Заклинательная характеристика блока")
    private Ability ability;

    /** Плоское число, как {@code attackBonus}/{@code saveDc} боевой механики: не выводится. */
    @Schema(description = "Сложность спасброска заклинаний блока", example = "12")
    private Integer saveDc;

    /** Тоже плоское число. */
    @Schema(description = "Бонус к броску атаки заклинанием", example = "4")
    private Integer attackBonus;

    @Schema(description = "Компоненты, которые блоку НЕ требуются")
    private CreatureSpellComponents ignoredComponents;

    @Schema(description = "Порции блока")
    private List<CreatureSpellGroup> groups;
}
