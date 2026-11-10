package club.ttg.dnd5.domain.bastion.player.activity;

import club.ttg.dnd5.domain.bastion.player.event.BastionEventDetails;
import club.ttg.dnd5.domain.bastion.player.event.PlayerBastionEvent;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionLabel;
import club.ttg.dnd5.domain.common.model.EntityRef;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Журналы бастиона: приказы, ходы, казна и события — свежие первыми.
 */
@Schema(description = "Журналы бастиона")
public record ActivityResponse(List<Order> orders, List<Turn> turns, List<LedgerEntry> ledger, List<Event> events) {

    /**
     * Приказ.
     *
     * @param facilityName название сооружения; пусто у «Обслуживать»
     * @param item         изготавливаемый предмет справочника; пусто, если не выбирался
     * @param characterName персонаж, от чьего имени отдан приказ: владелец сооружения, у «Обслуживать» —
     *                      персонаж отдавшего; пусто, если отдал мастер без персонажа в бастионе
     * @param givenAt      когда приказ отдан
     * @param canCancel    открывший может отменить приказ: он отдан в текущий ход
     * @param canWriteResult открывший может записать итог выполненного приказа: мастер или тот,
     *                       кто отдал приказ
     */
    @Schema(description = "Приказ")
    public record Order(UUID id,
                        UUID facilityId,
                        String facilityName,
                        BastionLabel order,
                        String optionName,
                        EntityRef item,
                        long costGp,
                        int givenOnTurn,
                        int completesOnTurn,
                        PlayerBastionOrder.OrderStatus status,
                        UUID givenBy,
                        String characterName,
                        Instant givenAt,
                        String note,
                        String result,
                        boolean canCancel,
                        boolean canWriteResult) {
    }

    @Schema(description = "Ход")
    public record Turn(int number, boolean maintain, String event, List<String> summary,
                       UUID performedBy, Instant performedAt) {
    }

    /**
     * Событие бастиона.
     *
     * @param roll      бросок 1к100 по таблице «События бастиона»
     * @param decisions решения, которые событие ждёт; пусто — разрешено само
     * @param canDecide открывший может принять решение: игрок персонажа или мастер
     * @param defenders сколько защитников персонажа можно отправить на помощь
     * @param noteUrl   полученный магический предмет из раздела «Магические предметы»
     */
    @Schema(description = "Событие бастиона")
    public record Event(UUID id,
                        UUID memberId,
                        String characterName,
                        int turn,
                        int roll,
                        BastionLabel type,
                        PlayerBastionEvent.Status status,
                        String summary,
                        BastionEventDetails details,
                        PlayerBastionEvent.Decision decision,
                        String note,
                        UUID parentId,
                        List<PlayerBastionEvent.Decision> decisions,
                        boolean canDecide,
                        int defenders,
                        @Schema(description = "Ссылка на полученный магический предмет; null — записан словами")
                        String noteUrl) {
    }

    /**
     * Движение казны.
     *
     * @param characterName персонаж того, кто провёл движение; пусто у мастера без персонажа в бастионе
     */
    @Schema(description = "Движение казны")
    public record LedgerEntry(int turn, long amountGp, PlayerBastionLedgerEntry.Reason reason, String note,
                              UUID createdBy, String characterName, Instant createdAt) {
    }
}
