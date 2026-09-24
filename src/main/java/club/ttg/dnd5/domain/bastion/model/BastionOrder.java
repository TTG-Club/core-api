package club.ttg.dnd5.domain.bastion.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Приказы бастиона.
 *
 * <p>Имена констант — публичный контракт: они лежат в JSONB сооружений и уйдут в состояние
 * мини-игры, поэтому переименовывать их молча нельзя.</p>
 */
@Getter
@AllArgsConstructor
public enum BastionOrder {
    CRAFT("Изготовить", "Craft",
            "Наёмники изготавливают предмет, который можно создать в этом сооружении."),
    EMPOWER("Усилить", "Empower",
            "Сооружение даёт вам или кому-то другому временные усиления."),
    HARVEST("Собирать", "Harvest",
            "Наёмники собирают ресурс, произведённый в сооружении."),
    MAINTAIN("Обслуживать", "Maintain",
            "Отдаётся всему бастиону: другие приказы в этот ход запрещены, Мастер бросает по таблице событий бастиона."),
    RECRUIT("Завербовать", "Recruit",
            "Наёмники набирают существ в бастион, например защитников бастиона."),
    RESEARCH("Изучать", "Research",
            "Наёмники собирают информацию."),
    TRADE("Торговать", "Trade",
            "Наёмники покупают и продают товары или услуги сооружения.");

    private final String name;
    private final String english;
    private final String description;

    /** Приказ отдаётся всему бастиону, а не отдельному сооружению. */
    public boolean isBastionWide() {
        return this == MAINTAIN;
    }
}
