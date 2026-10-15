package club.ttg.dnd5.domain.bastion.player.activity;

import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.model.MenagerieBeast;
import club.ttg.dnd5.domain.bastion.player.model.SelectedChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
     * @param cancelActive для «Обслуживать»: отменить приказы, которые сооружения ещё выполняют
     * @param itemUrl    для «Изготовить» в мастерской: предмет из тех, что создаются её инструментами
     * @param creature   для «Завербовать» в зверинце: какое существо добавить в вольеры
     */
    @Schema(description = "Приказ бастиона")
    public record GiveOrder(UUID facilityId,
                            @NotNull BastionOrder order,
                            @Size(max = 200) String optionName,
                            @Size(max = 2000) String note,
                            Boolean cancelActive,
                            @Size(max = 200) String itemUrl,
                            @Valid RecruitCreature creature) {
        public GiveOrder(UUID facilityId, BastionOrder order, String optionName, String note) {
            this(facilityId, order, optionName, note, null, null, null);
        }

        public GiveOrder(UUID facilityId, BastionOrder order, String optionName, String note,
                         Boolean cancelActive, String itemUrl) {
            this(facilityId, order, optionName, note, cancelActive, itemUrl, null);
        }
    }

    /**
     * Существо, которое вербует зверинец: из таблицы зверинца или, с согласия Мастера, любое
     * другое — тогда цена считается по показателю опасности.
     *
     * @param beast     существо из таблицы «Существа зверинца»
     * @param name      название существа не из таблицы
     * @param size      размер существа не из таблицы
     * @param challenge показатель опасности существа не из таблицы: «1/4», «2»
     */
    @Schema(description = "Существо для зверинца")
    public record RecruitCreature(MenagerieBeast beast,
                                  @Size(max = 100) String name,
                                  club.ttg.dnd5.domain.common.dictionary.Size size,
                                  @Size(max = 8) String challenge) {
    }

    /**
     * Считать ли существо зверинца защитником бастиона.
     *
     * @param defender {@code false} — существо не призывают на защиту, оно действует по своей природе
     */
    @Schema(description = "Существо зверинца — защитник бастиона")
    public record CreatureDefender(@NotNull Boolean defender) {
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
