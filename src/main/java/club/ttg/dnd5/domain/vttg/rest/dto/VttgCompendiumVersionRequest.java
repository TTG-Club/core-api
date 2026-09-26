package club.ttg.dnd5.domain.vttg.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Новая версия формата выгрузки VTTG — строго больше текущей. */
public record VttgCompendiumVersionRequest(
        @NotNull(message = "укажите версию")
        @PositiveOrZero(message = "версия не может быть отрицательной")
        Integer version) {
}
