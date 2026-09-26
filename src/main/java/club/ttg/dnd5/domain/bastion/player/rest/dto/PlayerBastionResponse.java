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
 * @param canManage можно менять название, состав, казну, подтверждать требования и делать ходы (мастер)
 * @param canEdit   можно рисовать план и отдавать приказы
 */
@Schema(description = "Бастион игроков")
public record PlayerBastionResponse(
        UUID id,
        UUID gameId,
        String name,
        PlayerBastionStatus status,
        int turn,
        long treasuryGp,
        long version,
        boolean canManage,
        boolean canEdit,
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
            boolean enlargeable) {
    }
}
