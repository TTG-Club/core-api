package club.ttg.dnd5.domain.bastion.player.activity;

import club.ttg.dnd5.domain.bastion.rest.dto.BastionLabel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Журналы бастиона: приказы, ходы, казна — свежие первыми.
 */
@Schema(description = "Журналы бастиона")
public record ActivityResponse(List<Order> orders, List<Turn> turns, List<LedgerEntry> ledger) {

    /**
     * Приказ.
     *
     * @param facilityName название сооружения; пусто у «Обслуживать»
     * @param canCancel    открывший может отменить приказ: он отдан в текущий ход
     */
    @Schema(description = "Приказ")
    public record Order(UUID id,
                        UUID facilityId,
                        String facilityName,
                        BastionLabel order,
                        String optionName,
                        long costGp,
                        int givenOnTurn,
                        int completesOnTurn,
                        PlayerBastionOrder.OrderStatus status,
                        UUID givenBy,
                        String note,
                        String result,
                        boolean canCancel) {
    }

    @Schema(description = "Ход")
    public record Turn(int number, boolean maintain, String event, List<String> summary,
                       UUID performedBy, Instant performedAt) {
    }

    @Schema(description = "Движение казны")
    public record LedgerEntry(int turn, long amountGp, PlayerBastionLedgerEntry.Reason reason, String note,
                              UUID createdBy, Instant createdAt) {
    }
}
