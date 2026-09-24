package club.ttg.dnd5.domain.bastion.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Вариант исполнения приказа в сооружении: «Изготовить: книгу», «Завербовать: лейтенанта»,
 * «Завербовать: существо (Лев)».
 *
 * <p>Числа вынесены из текста, чтобы мини-игра могла списать деньги и поставить таймер.
 * Если цена или срок не выражаются числом (половина стоимости яда, правила создания
 * магических предметов), число пустое, а правило — в {@link #costNote} и {@link #description}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Вариант приказа")
public class FacilityOrderOption {
    @Schema(description = "Приказ", requiredMode = Schema.RequiredMode.REQUIRED)
    private BastionOrder order;
    @Schema(description = "Название варианта", examples = {"книгу", "лейтенанта"})
    private String name;
    @Schema(description = "Описание варианта")
    private String description;
    @Schema(description = "Время выполнения, дни; null — зависит от предмета")
    private Integer days;
    @Schema(description = "Фиксированная стоимость, зм; 0 — бесплатно, null — считается по правилу")
    private Integer cost;
    @Schema(description = "Правило расчёта стоимости, если она не фиксирована",
            examples = {"100 зм + 100 зм за каждого защитника"})
    private String costNote;
    @Schema(description = "Минимальный уровень персонажа для варианта")
    private Integer minLevel;
}
