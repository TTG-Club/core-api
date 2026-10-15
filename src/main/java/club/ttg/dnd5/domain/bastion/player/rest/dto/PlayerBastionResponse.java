package club.ttg.dnd5.domain.bastion.player.rest.dto;

import club.ttg.dnd5.domain.bastion.player.model.FacilityStatus;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.model.SelectedChoice;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionLabel;
import club.ttg.dnd5.domain.bastion.rest.dto.FacilitySpaceResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Бастион глазами того, кто его открыл.
 *
 * @param gameId          игра, в которую добавлен бастион; {@code null} — бастион вне игры
 * @param ownerId         владелец — тот, кто создал бастион
 * @param owned           открывший — владелец: может переименовать, удалить и добавить в игру
 * @param canManage       можно менять состав, казну, подтверждать требования и делать ходы:
 *                        мастер игры, а вне игры — владелец
 * @param canEdit         можно рисовать план и отдавать приказы
 * @param canLeaveGame    можно убрать бастион из игры: владелец или мастер этой игры
 * @param shareToken      секрет ссылки «поделиться»; видит только владелец, {@code null} — ссылки нет
 * @param editRequests    сколько запросов на правку ждут ответа; считается только для владельца
 * @param editor          владелец разрешил открывшему править бастион по ссылке
 */
@Schema(description = "Бастион игроков")
public record PlayerBastionResponse(
        UUID id,
        UUID gameId,
        UUID ownerId,
        String name,
        PlayerBastionStatus status,
        int turn,
        long treasuryGp,
        long version,
        boolean owned,
        boolean canManage,
        boolean canEdit,
        boolean canLeaveGame,
        UUID shareToken,
        long editRequests,
        boolean editor,
        List<Member> members,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * Игрок с доступом и его персонаж.
     *
     * @param specialFacilityLimit сколько специализированных сооружений положено персонажу
     * @param canEditFacilities    открывший может менять сооружения этого персонажа: мастер или
     *                             сам игрок, пока бастион в закладке
     * @param basicComplete        выбраны оба стартовых базовых сооружения
     * @param canGiveOrders        открывший может отдавать приказы сооружениям персонажа,
     *                             строить и расширять их: бастион запущен, это мастер или сам игрок
     */
    @Schema(description = "Игрок с доступом к бастиону")
    public record Member(
            UUID id,
            UUID userId,
            String characterName,
            int characterLevel,
            UUID characterSheetId,
            int specialFacilityLimit,
            boolean canEditFacilities,
            boolean basicComplete,
            boolean canGiveOrders,
            List<Facility> facilities) {
    }

    /**
     * Сооружение персонажа вместе с данными справочника.
     *
     * @param prerequisiteConfirmed требование подтверждено мастером (у сооружений без требования — всегда)
     * @param hirelings             число наёмников сооружения
     * @param status                готово или строится
     * @param readyOnTurn           ход, на котором достроится
     * @param pendingSpace          пространство, до которого идёт расширение
     * @param pendingReadyOnTurn    ход, на котором закончится расширение
     * @param enlargeable           сооружение можно расширить сейчас: готово, не расширяется, и
     *                              расширять есть куда
     */
    @Schema(description = "Сооружение персонажа")
    public record Facility(
            UUID id,
            String facilityUrl,
            String name,
            String english,
            BastionLabel category,
            FacilitySpaceResponse space,
            Long level,
            Integer hirelings,
            List<BastionLabel> orders,
            BastionLabel prerequisite,
            boolean prerequisiteConfirmed,
            List<SelectedChoice> choices,
            FacilityStatus status,
            Integer readyOnTurn,
            FacilitySpaceResponse pendingSpace,
            Integer pendingReadyOnTurn,
            boolean enlargeable,
            @Schema(description = "Защитников в бараке; null — сооружение не барак") Integer defenders,
            @Schema(description = "Сколько защитников помещается в барак; null — не барак") Integer defenderCapacity,
            @Schema(description = "Что можно изготовить выбранными инструментами; null — у сооружения нет выбора инструментов")
            List<CraftableItem> craftableItems) {
    }

    /**
     * Предмет, который мастерская может изготовить.
     *
     * @param priceGp покупная стоимость, зм; null — в справочнике нет числовой цены
     * @param days    сколько дней изготавливать с наёмниками сооружения; null — цена неизвестна
     */
    @Schema(description = "Предмет для изготовления")
    public record CraftableItem(String url, String name, Double priceGp, Integer days) {
    }
}
