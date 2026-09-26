package club.ttg.dnd5.domain.vttg.rest.dto;

import java.time.Instant;

/**
 * Состояние фоновой пересборки выгрузки VTTG после смены версии компендиума. Живёт в памяти
 * экземпляра: после рестарта — {@link State#IDLE}.
 *
 * @param status     состояние пересборки
 * @param startedAt  начало последнего прогона
 * @param finishedAt конец последнего прогона ({@code null}, пока идёт)
 * @param error      краткий текст ошибки для админа ({@code null}, если ошибки нет)
 */
public record VttgRebuildStatus(State status, Instant startedAt, Instant finishedAt, String error) {

    public static final VttgRebuildStatus IDLE = new VttgRebuildStatus(State.IDLE, null, null, null);

    public enum State {
        IDLE, RUNNING, DONE, FAILED
    }
}
