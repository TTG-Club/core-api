package club.ttg.dnd5.domain.common.model.mechanics;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Каким отдыхом восстанавливается ресурс.
 *
 * <p>Значений ровно два: правила 2024 года не знают ресурсов, которые копятся иначе.
 * «Не восстанавливается» отдельным значением не заводится — такой счётчик и ресурсом
 * не является, его нечем откатывать.</p>
 */
@Getter
@AllArgsConstructor
public enum ResourceRecovery {
    SHORT_REST("короткий отдых"),
    LONG_REST("продолжительный отдых");

    private final String name;
}
