package club.ttg.dnd5.domain.bastion.player.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Броски и последствия события бастиона — хранятся в журнале как JSONB, чтобы событие
 * можно было показать и разрешить позже.
 */
@Getter
@Setter
@NoArgsConstructor
public class BastionEventDetails {
    /** Бросок по вложенной таблице: детали «Всё хорошо» (1к8), гость (1к4), сокровище (1к100). */
    private Integer detailRoll;

    /** Строка вложенной таблицы. */
    private String detail;

    /** Кости события: 6к6 атаки, 2к4 беженцев, к6 за отправленных защитников. */
    private List<Integer> dice = new ArrayList<>();

    /** Деньги события, зм: плата, взятка, награда. */
    private Long amountGp;

    /** Сооружение, которого коснулось событие. */
    private UUID facilityId;

    private String facilityName;

    /** Сколько: беженцев, отправленных защитников. */
    private Integer count;

    /** Кого бастион потерял. */
    private List<String> lost = new ArrayList<>();
}
