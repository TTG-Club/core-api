package club.ttg.dnd5.domain.feat.model.mechanics;

import club.ttg.dnd5.domain.common.model.EntityRef;
import club.ttg.dnd5.domain.spell.model.enums.CastingUnit;
import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * Чем ограничен выбор заклинания или заговора.
 *
 * <p>Поля повторяют параметры фильтра заклинаний, которыми описания черт уже пользуются
 * в ссылках вида {@code /spells?level=1&school=ILLUSION,NECROMANCY} и
 * {@code /spells?level=0&className=druid-phb,cleric-phb,wizard-phb} — так выбор в листе
 * приводит к тому же набору, что и ссылка в тексте.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SpellFilter {
    @Schema(description = "Точный уровень заклинания; 0 — заговор", example = "1")
    private Integer level;

    @Schema(description = "Наибольший допустимый уровень: «заклинание 8 уровня или ниже»", example = "8")
    private Integer maxLevel;

    @Schema(description = "Школы магии, любой из которых достаточно")
    private Set<MagicSchool> schools;

    @Schema(description = "Классы, из списков заклинаний которых можно выбирать")
    private List<EntityRef> classes;

    /**
     * Ключ выбора, из ответа на который берётся класс.
     *
     * <p>«Посвящённый в магию» сначала спрашивает список — жреца, друида или волшебника, —
     * и только потом даёт выбрать из него заговоры: пул сужается до одного выбранного
     * класса, а не до всех трёх, как было бы с {@link #classes}. Ссылка устроена так же,
     * как {@code fromChoiceKey} у повышения характеристик.</p>
     *
     * <p>Не задан — пул ограничен {@link #classes} напрямую.</p>
     */
    @Schema(description = "Ключ выбора, из ответа на который берётся класс", example = "spell-list")
    private String classesFromChoiceKey;

    /**
     * Время накладывания. {@link CastingUnit#RITUAL} — это заклинания со свойством ритуал
     * («Ритуальный заклинатель»), {@link CastingUnit#ACTION} — «Магия гениев».
     */
    @Schema(description = "Время накладывания", example = "ACTION")
    private CastingUnit castingTime;
}
