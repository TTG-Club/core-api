package club.ttg.dnd5.domain.bastion.player.activity;

import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.player.model.SelectedChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Запросы журналов бастиона: приказы, ход, казна, стройка. */
public final class ActivityRequests {
    private ActivityRequests() {
    }

    /**
     * Приказ бастиона.
     *
     * @param facilityId сооружение персонажа; пусто — «Обслуживать» всему бастиону
     * @param order      приказ
     * @param optionName вариант приказа из справочника: «книгу», «лейтенанта»
     * @param note       пояснение: тема исследования, что изготовить
     */
    @Schema(description = "Приказ бастиона")
    public record GiveOrder(UUID facilityId,
                            @NotNull BastionOrder order,
                            @Size(max = 200) String optionName,
                            @Size(max = 2000) String note) {
    }

    /**
     * Ход бастиона.
     *
     * @param event   событие бастиона при обслуживании — мастер бросает по таблице за столом
     * @param results итоги завершающихся приказов: приказ → текст
     */
    @Schema(description = "Ход бастиона")
    public record Turn(@Size(max = 4000) String event, Map<UUID, @Size(max = 4000) String> results) {
    }

    /**
     * Движение казны мастером.
     *
     * @param amountGp плюс — пополнение, минус — списание, зм
     * @param note     за что
     */
    @Schema(description = "Движение казны")
    public record Treasury(@NotNull Long amountGp, @Size(max = 500) String note) {
    }

    /**
     * Постройка базового сооружения за деньги и время.
     *
     * @param facilityUrl базовое сооружение справочника
     * @param space       пространство: цена и срок — из правил
     */
    @Schema(description = "Постройка базового сооружения")
    public record BuildBasic(@NotBlank String facilityUrl, @NotNull FacilitySpace space) {
    }

    /**
     * Новое специализированное сооружение — персонаж дорос до следующего лимита.
     *
     * @param facilityUrl сооружение справочника
     * @param choices     выборы: тип сада, мастер
     */
    @Schema(description = "Новое специализированное сооружение")
    public record AddSpecial(@NotBlank String facilityUrl, List<SelectedChoice> choices) {
    }
}
