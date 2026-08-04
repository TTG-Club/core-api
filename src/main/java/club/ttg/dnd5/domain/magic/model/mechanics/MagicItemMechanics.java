package club.ttg.dnd5.domain.magic.model.mechanics;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Механика влияния магического предмета на лист персонажа.
 *
 * <p>Сами изменения описывает {@link ActiveEffect} — та же модель, что у эффектов
 * заклинания, в вокабуляре VTTG. Плащ защиты — это два изменения
 * ({@code armorClass} и {@code save.*} режимом {@code add}), амулет здоровья —
 * одно ({@code ability.constitution} режимом {@code upgrade} до 19), плащ ската
 * — {@code movement.swim} тем же {@code upgrade}. Ситуационность («+2 КД против
 * дальнобойных атак») живёт в {@code Change.condition}.</p>
 *
 * <p>Остальные поля своих аналогов в VTTG не имеют и в экспорт не идут: там
 * эффекты предмета включаются одним признаком «экипирован», а зарядов предмета
 * система не знает.</p>
 *
 * <p>{@code null} — у записей, сохранённых до появления поля, и у предметов, чьё
 * действие пока описано только текстом.</p>
 */
@Getter
@Setter
public class MagicItemMechanics {
    @Schema(description = "Когда механика работает", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private MagicItemActivation activation;

    @Schema(description = "Изменения, которые предмет вносит в лист персонажа (модель Active Effects VTTG)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<ActiveEffect> activeEffects;

    @Schema(description = "Заряды предмета", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private MagicItemResource resource;

    /**
     * Свойства, которые лист показывает справкой, но не считает: дыхание под водой,
     * иммунитет к чтению мыслей и прочее, чему пока нет места в формулах.
     */
    @Schema(description = "Пассивные свойства для листа, не участвующие в расчётах",
            examples = {"Вы можете дышать под водой"},
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String passive;
}
